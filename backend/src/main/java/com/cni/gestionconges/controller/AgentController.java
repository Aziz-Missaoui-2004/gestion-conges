package com.cni.gestionconges.controller;

import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.LeaveBalance;
import com.cni.gestionconges.repositories.AgentRepository;
import com.cni.gestionconges.repositories.LeaveBalanceRepository;
import com.cni.gestionconges.dto.AgentDashboardResponse;
import com.cni.gestionconges.service.LeaveAccrualService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentRepository agentRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveAccrualService leaveAccrualService;

    public AgentController(
            AgentRepository agentRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            LeaveAccrualService leaveAccrualService) {
        this.agentRepository = agentRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveAccrualService = leaveAccrualService;
    }

    @GetMapping("/me")
    public Agent getCurrentAgent(@AuthenticationPrincipal Jwt jwt) {
        return getAgentFromJwt(jwt);
    }

    @GetMapping("/me/balance/{annee}")
    public LeaveBalance getCurrentBalance(
            @PathVariable Integer annee,
            @AuthenticationPrincipal Jwt jwt) {
        int currentYear = java.time.LocalDate.now().getYear();
        if (annee == null || annee > currentYear) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les soldes des années futures ne sont pas disponibles");
        }
        Agent agent = getAgentFromJwt(jwt);
        LeaveBalance balance = getOrCreateBalance(agent, annee);
        java.time.LocalDate calculationDate = annee == currentYear
                ? java.time.LocalDate.now()
                : java.time.LocalDate.of(annee, 12, 31);
        balance.setJoursAcquis(
                leaveAccrualService.calculate(agent, annee, calculationDate).daysAcquired()
                        + balance.getJoursAjustement());
        return leaveBalanceRepository.save(balance);
    }

    @GetMapping("/me/dashboard")
    public AgentDashboardResponse getDashboard(@AuthenticationPrincipal Jwt jwt) {
        Agent agent = getAgentFromJwt(jwt);
        int year = java.time.LocalDate.now().getYear();
        LeaveBalance balance = getOrCreateBalance(agent, year);
        LeaveAccrualService.AccrualSummary accrual =
                leaveAccrualService.calculate(agent, year, java.time.LocalDate.now());

        balance.setJoursAcquis(accrual.daysAcquired() + balance.getJoursAjustement());
        leaveBalanceRepository.save(balance);

        AgentDashboardResponse response = new AgentDashboardResponse();
        response.setAgentId(agent.getId());
        response.setNom(agent.getNom());
        response.setPrenom(agent.getPrenom());
        response.setEmail(agent.getUser().getEmail());
        response.setDateEmbauche(agent.getDateEmbauche() == null
                ? null : agent.getDateEmbauche().toString());
        response.setMoisAcquis(accrual.monthsAcquired());
        response.setJoursTravailles(accrual.workedDays());
        response.setJoursAcquis(balance.getJoursAcquis());
        response.setJoursConsommes(balance.getJoursConsommes());
        response.setJoursDisponibles(
                balance.getJoursAcquis() - balance.getJoursConsommes());
        response.setAnnee(year);
        return response;
    }

    private LeaveBalance getOrCreateBalance(Agent agent, int year) {
        return leaveBalanceRepository.findByAgentAndAnnee(agent, year)
                .orElseGet(() -> {
                    LeaveBalance balance = new LeaveBalance();
                    balance.setAgent(agent);
                    balance.setAnnee(year);
                    balance.setJoursAcquis(0.0);
                    balance.setJoursConsommes(0.0);
                    return leaveBalanceRepository.save(balance);
                });
    }

    private Agent getAgentFromJwt(Jwt jwt) {
        return agentRepository.findByUser_Email(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Agent introuvable"));
    }
}
