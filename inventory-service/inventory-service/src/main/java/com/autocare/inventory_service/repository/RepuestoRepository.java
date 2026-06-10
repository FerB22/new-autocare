package com.autocare.inventory_service.repository;

import com.autocare.inventory_service.model.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {
    Optional<Repuesto> findByCodigoSku(String codigoSku);
}