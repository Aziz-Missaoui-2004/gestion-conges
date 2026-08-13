package com.cni.gestionconges.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"agent_id", "annee"}
        )
)

public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent;

    @Column(nullable = false)
    private Integer annee;

    @Column(nullable = false)
    private Double joursAcquis = 0.0;

    @Column(nullable = false)
    private Double joursConsommes = 0.0;

    @Column
    private Double joursAjustement = 0.0;

    public LeaveBalance() {
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

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public Double getJoursAcquis() {
        return joursAcquis;
    }

    public void setJoursAcquis(Double joursAcquis) {
        this.joursAcquis = joursAcquis;
    }

    public Double getJoursConsommes() {
        return joursConsommes;
    }

    public void setJoursConsommes(Double joursConsommes) {
        this.joursConsommes = joursConsommes;
    }

    public Double getJoursAjustement() {
        return joursAjustement == null ? 0.0 : joursAjustement;
    }

    public void setJoursAjustement(Double joursAjustement) {
        this.joursAjustement = joursAjustement;
    }
}
