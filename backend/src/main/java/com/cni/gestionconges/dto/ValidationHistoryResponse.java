package com.cni.gestionconges.dto;

import com.cni.gestionconges.entity.ValidationDecision;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ValidationHistoryResponse {
    private Long validationId;
    private Long leaveRequestId;
    private String agentNom;
    private String agentPrenom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nombreJours;
    private Integer niveau;
    private ValidationDecision decision;
    private String commentaire;
    private LocalDateTime dateDecision;

    public Long getValidationId() { return validationId; }
    public void setValidationId(Long validationId) { this.validationId = validationId; }
    public Long getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; }
    public String getAgentNom() { return agentNom; }
    public void setAgentNom(String agentNom) { this.agentNom = agentNom; }
    public String getAgentPrenom() { return agentPrenom; }
    public void setAgentPrenom(String agentPrenom) { this.agentPrenom = agentPrenom; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public Integer getNombreJours() { return nombreJours; }
    public void setNombreJours(Integer nombreJours) { this.nombreJours = nombreJours; }
    public Integer getNiveau() { return niveau; }
    public void setNiveau(Integer niveau) { this.niveau = niveau; }
    public ValidationDecision getDecision() { return decision; }
    public void setDecision(ValidationDecision decision) { this.decision = decision; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }
}
