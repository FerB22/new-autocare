package com.autocare.diagnostics_service.repository;

import com.autocare.diagnostics_service.model.ReporteDiagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReporteDiagnosticoRepository extends JpaRepository<ReporteDiagnostico, Long> {
    List<ReporteDiagnostico> findByVehiculoId(Long vehiculoId);
}