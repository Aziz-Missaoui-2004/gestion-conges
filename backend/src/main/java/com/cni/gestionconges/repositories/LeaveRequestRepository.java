package com.cni.gestionconges.repositories;

import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.LeaveRequest;
import com.cni.gestionconges.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByAgent(Agent agent);

    List<LeaveRequest> findByAgentAndStatut(
            Agent agent,
            LeaveStatus statut
    );

    List<LeaveRequest> findByProchainValidateurAndStatut(
            Agent prochainValidateur,
            LeaveStatus statut
    );

    List<LeaveRequest> findByStatut(LeaveStatus statut);

    boolean existsByAgentAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            Agent agent,
            java.time.LocalDate dateFin,
            java.time.LocalDate dateDebut
    );

    boolean existsByAgentAndStatutInAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(
            Agent agent,
            List<LeaveStatus> statuts,
            java.time.LocalDate dateFin,
            java.time.LocalDate dateDebut
    );
}
