package com.cni.gestionconges.dto;

public class AgentDashboardResponse {
    private Long agentId;
    private String nom;
    private String prenom;
    private String email;
    private String dateEmbauche;
    private int moisAcquis;
    private long joursTravailles;
    private double joursAcquis;
    private double joursConsommes;
    private double joursDisponibles;
    private int annee;

    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(String dateEmbauche) { this.dateEmbauche = dateEmbauche; }
    public int getMoisAcquis() { return moisAcquis; }
    public void setMoisAcquis(int moisAcquis) { this.moisAcquis = moisAcquis; }
    public long getJoursTravailles() { return joursTravailles; }
    public void setJoursTravailles(long joursTravailles) { this.joursTravailles = joursTravailles; }
    public double getJoursAcquis() { return joursAcquis; }
    public void setJoursAcquis(double joursAcquis) { this.joursAcquis = joursAcquis; }
    public double getJoursConsommes() { return joursConsommes; }
    public void setJoursConsommes(double joursConsommes) { this.joursConsommes = joursConsommes; }
    public double getJoursDisponibles() { return joursDisponibles; }
    public void setJoursDisponibles(double joursDisponibles) { this.joursDisponibles = joursDisponibles; }
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
}
