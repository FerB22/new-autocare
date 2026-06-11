package com.autocare.booking_service.repository;

import com.autocare.booking_service.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> { // Cambiado a Long
    List<Cita> findByVehiculoId(Long vehiculoId);
}