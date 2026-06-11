package com.autocare.procurement_service.repository;

import com.autocare.procurement_service.model.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByEstado(OrdenCompra.EstadoOrden estado);
}