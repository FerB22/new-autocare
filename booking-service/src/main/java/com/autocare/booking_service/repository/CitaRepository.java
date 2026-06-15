package com.autocare.booking_service.repository;

import com.autocare.booking_service.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> { 
    
    List<Cita> findByVehiculoId(Long vehiculoId);

    // Consulta nativa para PostgreSQL: extrae solo la fecha del campo fecha_hora
    @Query(value = "SELECT COUNT(*) FROM citas WHERE DATE(fecha_hora) = :fecha", nativeQuery = true)
    long countByFecha(@Param("fecha") LocalDate fecha);

    // Consulta nativa para PostgreSQL: verifica fecha y hora exacta
    @Query(value = "SELECT EXISTS(SELECT 1 FROM citas WHERE DATE(fecha_hora) = :fecha AND CAST(fecha_hora AS TIME) = :hora)", nativeQuery = true)
    boolean existsByFechaAndHora(@Param("fecha") LocalDate fecha, @Param("hora") LocalTime hora);
}