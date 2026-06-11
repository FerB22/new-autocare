package com.autocare.analytics_service.repository;

import com.autocare.analytics_service.model.ReporteMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReporteMensualRepository extends JpaRepository<ReporteMensual, Long> {
    Optional<ReporteMensual> findByMesAndAnio(Integer mes, Integer anio);
}