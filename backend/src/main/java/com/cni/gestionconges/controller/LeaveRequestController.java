package com.cni.gestionconges.controller;

import com.cni.gestionconges.dto.CreateLeaveRequestRequest;
import com.cni.gestionconges.dto.ValidationRequest;
import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.LeaveRequest;
import com.cni.gestionconges.entity.LeaveStatus;
import com.cni.gestionconges.entity.Validation;
import com.cni.gestionconges.repositories.AgentRepository;
import com.cni.gestionconges.repositories.LeaveRequestRepository;
import com.cni.gestionconges.service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestRepository leaveRequestRepository;
    private final AgentRepository agentRepository;
    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(
            LeaveRequestRepository leaveRequestRepository,
            AgentRepository agentRepository,
            LeaveRequestService leaveRequestService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.agentRepository = agentRepository;
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping
    public List<LeaveRequest> getPendingForManager(@AuthenticationPrincipal Jwt jwt) {
        Agent manager = getAgentFromJwt(jwt);
        for (LeaveRequest pending : leaveRequestRepository.findByStatut(LeaveStatus.EN_ATTENTE)) {
            if (pending.getProchainValidateur() == null
                    && pending.getAgent().getResponsableDirect() != null) {
                pending.setProchainValidateur(pending.getAgent().getResponsableDirect());
                pending.setNiveauValidation(1);
                leaveRequestRepository.save(pending);
            }
        }
        return leaveRequestRepository.findByProchainValidateurAndStatut(
                manager, LeaveStatus.EN_ATTENTE);
    }

    @GetMapping("/{id}")
    public LeaveRequest getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demande introuvable"));
        Agent currentAgent = getAgentFromJwt(jwt);
        boolean owner = request.getAgent().getId().equals(currentAgent.getId());
        boolean currentValidator = request.getProchainValidateur() != null
                && request.getProchainValidateur().getId().equals(currentAgent.getId());
        if (!owner && !currentValidator) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez pas consulter cette demande");
        }
        return request;
    }

    @PostMapping
    public LeaveRequest create(
            @RequestBody CreateLeaveRequestRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Agent agent = getAgentFromJwt(jwt);

        if (agent.getResponsableDirect() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aucun responsable n'est configuré pour cet agent");
        }
        if (request.getDateDebut() == null || request.getDateFin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Erreur de saisie : vérifiez les dates renseignées");
        }
        if (request.getDateFin().isBefore(request.getDateDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Erreur de saisie : la date de fin doit être après la date de début");
        }
        if (request.getDateDebut().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Durée passée : la date de début ne peut pas être antérieure à aujourd'hui");
        }
        if (request.getDateDebut().getYear() != LocalDate.now().getYear()
                || request.getDateFin().getYear() != LocalDate.now().getYear()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Période non autorisée : les demandes pour une année future ne sont pas encore disponibles");
        }
        boolean overlap = leaveRequestRepository
                .existsByAgentAndStatutInAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                        agent,
                        List.of(LeaveStatus.EN_ATTENTE, LeaveStatus.VALIDEE),
                        request.getDateFin(),
                        request.getDateDebut());
        if (overlap) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Période déjà demandée ou déjà validée : choisissez une autre période");
        }

        int nombreJours = (int) ChronoUnit.DAYS.between(
                request.getDateDebut(), request.getDateFin()) + 1;
        leaveRequestService.ensureAvailableBalance(
                agent,
                request.getDateDebut().getYear(),
                nombreJours);

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setAgent(agent);
        leaveRequest.setDateDebut(request.getDateDebut());
        leaveRequest.setDateFin(request.getDateFin());
        leaveRequest.setNombreJours(nombreJours);
        leaveRequest.setMotif(request.getMotif());
        leaveRequest.setStatut(LeaveStatus.EN_ATTENTE);
        leaveRequest.setProchainValidateur(agent.getResponsableDirect());
        leaveRequest.setNiveauValidation(1);
        return leaveRequestRepository.save(leaveRequest);
    }

    @GetMapping("/my")
    public List<LeaveRequest> getMyRequests(@AuthenticationPrincipal Jwt jwt) {
        return leaveRequestRepository.findByAgent(getAgentFromJwt(jwt));
    }

    @PostMapping("/{id}/approve")
    public LeaveRequest approve(
            @PathVariable Long id,
            @RequestBody(required = false) ValidationRequest validation,
            @AuthenticationPrincipal Jwt jwt) {
        Agent manager = getAgentFromJwt(jwt);
        return leaveRequestService.approve(id, manager,
                validation == null ? null : validation.getCommentaire());
    }

    @PostMapping("/{id}/reject")
    public LeaveRequest reject(
            @PathVariable Long id,
            @RequestBody(required = false) ValidationRequest validation,
            @AuthenticationPrincipal Jwt jwt) {
        Agent manager = getAgentFromJwt(jwt);
        return leaveRequestService.reject(id, manager,
                validation == null ? null : validation.getCommentaire());
    }

    @PostMapping("/{id}/cancel")
    public LeaveRequest cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (!request.getAgent().getUser().getEmail().equals(jwt.getSubject())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez pas annuler la demande d'un autre agent");
        }
        return leaveRequestService.cancel(id);
    }

    @GetMapping("/{id}/history")
    public List<Validation> getHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Demande introuvable"));
        Agent currentAgent = getAgentFromJwt(jwt);
        boolean owner = request.getAgent().getId().equals(currentAgent.getId());
        boolean currentValidator = request.getProchainValidateur() != null
                && request.getProchainValidateur().getId().equals(currentAgent.getId());
        if (!owner && !currentValidator) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous ne pouvez pas consulter cet historique");
        }
        return leaveRequestService.getHistory(request);
    }

    private Agent getAgentFromJwt(Jwt jwt) {
        return agentRepository.findByUser_Email(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agent introuvable"));
    }
}
