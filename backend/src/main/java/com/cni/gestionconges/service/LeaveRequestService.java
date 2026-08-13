package com.cni.gestionconges.service;

import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.LeaveBalance;
import com.cni.gestionconges.entity.LeaveRequest;
import com.cni.gestionconges.entity.LeaveStatus;
import com.cni.gestionconges.entity.Role;
import com.cni.gestionconges.entity.Validation;
import com.cni.gestionconges.entity.ValidationDecision;
import com.cni.gestionconges.repositories.LeaveBalanceRepository;
import com.cni.gestionconges.repositories.LeaveRequestRepository;
import com.cni.gestionconges.repositories.ValidationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final ValidationRepository validationRepository;
    private final LeaveAccrualService leaveAccrualService;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            ValidationRepository validationRepository,
            LeaveAccrualService leaveAccrualService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.validationRepository = validationRepository;
        this.leaveAccrualService = leaveAccrualService;
    }

    @Transactional
    public LeaveRequest approve(Long requestId, Agent validateur, String commentaire) {
        LeaveRequest request = loadPendingRequest(requestId);
        ensureExpectedValidator(request, validateur);
        recordDecision(request, validateur, ValidationDecision.APPROUVEE, commentaire);

        Agent nextValidator = validateur.getResponsableDirect();
        if (nextValidator != null) {
            if (nextValidator.getUser().getRole() != Role.RESPONSABLE) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Le responsable suivant n'a pas le rôle RESPONSABLE");
            }
            request.setProchainValidateur(nextValidator);
            request.setNiveauValidation(request.getNiveauValidation() + 1);
        } else {
            consumeBalance(request);
            request.setStatut(LeaveStatus.VALIDEE);
            request.setProchainValidateur(null);
        }

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest reject(Long requestId, Agent validateur, String commentaire) {
        LeaveRequest request = loadPendingRequest(requestId);
        ensureExpectedValidator(request, validateur);
        recordDecision(request, validateur, ValidationDecision.REFUSEE, commentaire);
        request.setStatut(LeaveStatus.REFUSEE);
        request.setProchainValidateur(null);
        return leaveRequestRepository.save(request);
    }

    @Transactional
    public LeaveRequest cancel(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demande introuvable"));

        if (request.getStatut() != LeaveStatus.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seule une demande en attente peut être annulée");
        }
        if (!request.getDateDebut().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le congé a déjà commencé");
        }

        request.setStatut(LeaveStatus.ANNULEE);
        request.setProchainValidateur(null);
        return leaveRequestRepository.save(request);
    }

    public void ensureAvailableBalance(
            Agent agent,
            int year,
            int requestedDays) {
        double acquired = leaveAccrualService.calculate(
                agent, year, LocalDate.now()).daysAcquired();
        double adjustment = leaveBalanceRepository.findByAgentAndAnnee(agent, year)
                .map(LeaveBalance::getJoursAjustement)
                .orElse(0.0);
        acquired += adjustment;
        double consumed = leaveBalanceRepository.findByAgentAndAnnee(agent, year)
                .map(LeaveBalance::getJoursConsommes)
                .orElse(0.0);
        int pendingDays = leaveRequestRepository
                .findByAgentAndStatut(agent, LeaveStatus.EN_ATTENTE)
                .stream()
                .filter(request -> request.getDateDebut().getYear() == year)
                .mapToInt(LeaveRequest::getNombreJours)
                .sum();

        double available = acquired - consumed - pendingDays;
        if (available < requestedDays) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solde de congés insuffisant pour cette demande");
        }
    }

    public List<Validation> getHistory(LeaveRequest request) {
        return validationRepository.findByLeaveRequestOrderByNiveauAsc(request);
    }

    private LeaveRequest loadPendingRequest(Long requestId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demande introuvable"));

        if (request.getStatut() != LeaveStatus.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette demande a déjà été traitée");
        }
        return request;
    }

    private void ensureExpectedValidator(LeaveRequest request, Agent validateur) {
        if (request.getProchainValidateur() == null
                || !request.getProchainValidateur().getId().equals(validateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas le validateur attendu de cette demande");
        }
    }

    private void recordDecision(
            LeaveRequest request,
            Agent validateur,
            ValidationDecision decision,
            String commentaire) {
        Validation validation = new Validation();
        validation.setLeaveRequest(request);
        validation.setValidateur(validateur);
        validation.setNiveau(request.getNiveauValidation());
        validation.setDecision(decision);
        validation.setCommentaire(commentaire);
        validationRepository.save(validation);
    }

    private void consumeBalance(LeaveRequest request) {
        int annee = request.getDateDebut().getYear();
        LeaveBalance balance = leaveBalanceRepository.findByAgentAndAnnee(
                        request.getAgent(), annee)
                .orElseGet(() -> {
                    LeaveBalance newBalance = new LeaveBalance();
                    newBalance.setAgent(request.getAgent());
                    newBalance.setAnnee(annee);
                    newBalance.setJoursAcquis(0.0);
                    newBalance.setJoursConsommes(0.0);
                    return leaveBalanceRepository.save(newBalance);
                });

        double acquired = leaveAccrualService.calculate(
                request.getAgent(), annee, LocalDate.now()).daysAcquired();
        acquired += balance.getJoursAjustement();
        balance.setJoursAcquis(acquired);
        double disponible = acquired - balance.getJoursConsommes();
        if (disponible < request.getNombreJours()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solde de congés insuffisant");
        }
        balance.setJoursConsommes(balance.getJoursConsommes() + request.getNombreJours());
        leaveBalanceRepository.save(balance);
    }
}
