// ServiceRepository.java
package com.cni.gestionconges.repositories;

import com.cni.gestionconges.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    boolean existsByNomIgnoreCase(String nom);
}
