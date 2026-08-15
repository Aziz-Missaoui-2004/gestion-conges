package com.cni.gestionconges.controller;

import com.cni.gestionconges.dto.ValidationHistoryResponse;
import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.Validation;
import com.cni.gestionconges.repositories.AgentRepository;
import com.cni.gestionconges.repositories.ValidationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/validations")
public class ValidationController {

    private final AgentRepository agentRepository;
    private final ValidationRepository validationRepository;

    public ValidationController(
            AgentRepository agentRepository,
            ValidationRepository validationRepository) {
        this.agentRepository = agentRepository;
        this.validationRepository = validationRepository;
    }

    @GetMapping("/my")
    public List<ValidationHistoryResponse> getMyValidations(
            @AuthenticationPrincipal Jwt jwt) {
        Agent manager = agentRepository.findByUser_Email(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Responsable introuvable"));

        return validationRepository.findByValidateurOrderByDateDecisionDesc(manager)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ValidationHistoryResponse toResponse(Validation validation) {
        ValidationHistoryResponse response = new ValidationHistoryResponse();
        response.setValidationId(validation.getId());
        response.setLeaveRequestId(validation.getLeaveRequest().getId());
        response.setAgentId(validation.getLeaveRequest().getAgent().getId());
        response.setAgentNom(validation.getLeaveRequest().getAgent().getNom());
        response.setAgentPrenom(validation.getLeaveRequest().getAgent().getPrenom());
        response.setDateDebut(validation.getLeaveRequest().getDateDebut());
        response.setDateFin(validation.getLeaveRequest().getDateFin());
        response.setNombreJours(validation.getLeaveRequest().getNombreJours());
        response.setNiveau(validation.getNiveau());
        response.setDecision(validation.getDecision());
        response.setCommentaire(validation.getCommentaire());
        response.setDateDecision(validation.getDateDecision());
        return response;
    }
}
