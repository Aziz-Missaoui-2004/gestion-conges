package com.cni.gestionconges.repositories;

import com.cni.gestionconges.entity.Agent;
import com.cni.gestionconges.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByAgentAndAnnee(Agent agent, Integer annee);
}