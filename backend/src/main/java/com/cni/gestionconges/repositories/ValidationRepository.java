package com.cni.gestionconges.repositories;

import com.cni.gestionconges.entity.LeaveRequest;
import com.cni.gestionconges.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.cni.gestionconges.entity.Agent;

public interface ValidationRepository extends JpaRepository<Validation, Long> {
    List<Validation> findByLeaveRequestOrderByNiveauAsc(LeaveRequest leaveRequest);

    List<Validation> findByValidateurOrderByDateDecisionDesc(Agent validateur);
}
