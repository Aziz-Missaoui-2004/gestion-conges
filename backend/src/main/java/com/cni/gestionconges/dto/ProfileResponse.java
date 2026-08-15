package com.cni.gestionconges.dto;

public class ProfileResponse {

    private Long userId;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String token;

    public ProfileResponse(Long userId, String nom, String prenom, String email,
                           String telephone, String token) {
        this.userId = userId;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.token = token;
    }

    public Long getUserId() { return userId; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getToken() { return token; }
}
