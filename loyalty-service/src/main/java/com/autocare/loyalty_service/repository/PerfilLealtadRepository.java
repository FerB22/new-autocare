package com.autocare.loyalty_service.repository;

import com.autocare.loyalty_service.model.PerfilLealtad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PerfilLealtadRepository extends JpaRepository<PerfilLealtad, Long> {
    Optional<PerfilLealtad> findByClienteId(Long clienteId);
}