package com.autocare.garage_service.repository;

import com.autocare.garage_service.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    Optional<Vehiculo> findByPatente(String patente);
}