package com.cni.gestionconges.dto;

public class UpdateLeaveBalanceRequest {
    private Integer annee;
    private Double joursAjustement;

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }
    public Double getJoursAjustement() { return joursAjustement; }
    public void setJoursAjustement(Double joursAjustement) {
        this.joursAjustement = joursAjustement;
    }
}
