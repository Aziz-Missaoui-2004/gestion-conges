package com.cni.gestionconges.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    private LocalDate dateEmbauche;

    private String telephone;

    @Enumerated(EnumType.STRING)
    private AgentStatus statut = AgentStatus.ACTIF;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private Service service;

    @ManyToOne
    @JoinColumn(name = "responsable_direct_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("responsableDirect")
    private Agent responsableDirect;

    public Agent() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public AgentStatus getStatut() {
        return statut == null ? AgentStatus.ACTIF : statut;
    }

    public void setStatut(AgentStatus statut) {
        this.statut = statut;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Agent getResponsableDirect() {
        return responsableDirect;
    }

    public void setResponsableDirect(Agent responsableDirect) {
        this.responsableDirect = responsableDirect;
    }
}
