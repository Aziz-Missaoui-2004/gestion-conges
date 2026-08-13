package com.cni.gestionconges.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validations")
public class Validation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "leave_request_id", nullable = false)
    @JsonIgnore
    private LeaveRequest leaveRequest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "validateur_id", nullable = false)
    private Agent validateur;

    @Column(nullable = false)
    private Integer niveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationDecision decision;

    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateDecision = LocalDateTime.now();

    public Long getId() { return id; }
    public LeaveRequest getLeaveRequest() { return leaveRequest; }
    public void setLeaveRequest(LeaveRequest leaveRequest) { this.leaveRequest = leaveRequest; }
    public Agent getValidateur() { return validateur; }
    public void setValidateur(Agent validateur) { this.validateur = validateur; }
    public Integer getNiveau() { return niveau; }
    public void setNiveau(Integer niveau) { this.niveau = niveau; }
    public ValidationDecision getDecision() { return decision; }
    public void setDecision(ValidationDecision decision) { this.decision = decision; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getDateDecision() { return dateDecision; }
}
