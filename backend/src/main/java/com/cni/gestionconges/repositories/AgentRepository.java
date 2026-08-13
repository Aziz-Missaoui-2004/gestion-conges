package com.cni.gestionconges.repositories;

import com.cni.gestionconges.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findByUserId(Long userId);

    Optional<Agent> findByUser_Email(String email);

    List<Agent> findByUser_Role(com.cni.gestionconges.entity.Role role);
}
