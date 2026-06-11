package com.autocare.diagnostics_service.service;

import com.autocare.diagnostics_service.dto.DiagnosticoRequestDTO;
import com.autocare.diagnostics_service.model.ReporteDiagnostico;
import com.autocare.diagnostics_service.repository.ReporteDiagnosticoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticoService {

    private final ReporteDiagnosticoRepository repository;

    public ReporteDiagnostico registrarDiagnostico(DiagnosticoRequestDTO dto) {
        ReporteDiagnostico reporte = new ReporteDiagnostico(
                null,
                dto.vehiculoId(),
                dto.mecanicoId(),
                dto.kilometrajeActual(),
                dto.codigosScanner(),
                dto.observacionesVisuales(),
                LocalDateTime.now()
        );
        return repository.save(reporte);
    }

    public List<ReporteDiagnostico> obtenerHistorialPorVehiculo(Long vehiculoId) {
        return repository.findByVehiculoId(vehiculoId);
    }
}