package com.cni.gestionconges.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private Integer nombreJours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus statut = LeaveStatus.EN_ATTENTE;

    private String motif;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "prochain_validateur_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("responsableDirect")
    private Agent prochainValidateur;

    @Column
    private Integer niveauValidation = 1;

    public LeaveRequest() {
    }

    public Long getId() {
        return id;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Integer getNombreJours() {
        return nombreJours;
    }

    public void setNombreJours(Integer nombreJours) {
        this.nombreJours = nombreJours;
    }

    public LeaveStatus getStatut() {
        return statut;
    }

    public void setStatut(LeaveStatus statut) {
        this.statut = statut;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Agent getProchainValidateur() {
        return prochainValidateur;
    }

    public void setProchainValidateur(Agent prochainValidateur) {
        this.prochainValidateur = prochainValidateur;
    }

    public Integer getNiveauValidation() {
        return niveauValidation == null ? 1 : niveauValidation;
    }

    public void setNiveauValidation(Integer niveauValidation) {
        this.niveauValidation = niveauValidation;
    }
}
