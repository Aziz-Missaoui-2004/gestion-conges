package com.cni.gestionconges.dto;

import com.cni.gestionconges.entity.AgentStatus;

public class UpdateAgentStatusRequest {
    private AgentStatus statut;

    public AgentStatus getStatut() {
        return statut;
    }

    public void setStatut(AgentStatus statut) {
        this.statut = statut;
    }
}
