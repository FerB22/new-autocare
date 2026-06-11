package com.autocare.billing_service.repository;

import com.autocare.billing_service.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    
    // Spring Data JPA genera las consultas SQL automáticamente basándose en estos nombres
    List<Factura> findByEstado(Factura.EstadoPago estado);
    
    Optional<Factura> findByOrdenTrabajoId(Long ordenTrabajoId);
}