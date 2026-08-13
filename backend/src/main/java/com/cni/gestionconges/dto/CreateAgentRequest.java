package com.cni.gestionconges.dto;

import com.cni.gestionconges.entity.Role;

public class CreateAgentRequest {

    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Long serviceId;
    private Long responsableDirectId;
    private Role role = Role.AGENT;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getResponsableDirectId() { return responsableDirectId; }
    public void setResponsableDirectId(Long responsableDirectId) {
        this.responsableDirectId = responsableDirectId;
    }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
