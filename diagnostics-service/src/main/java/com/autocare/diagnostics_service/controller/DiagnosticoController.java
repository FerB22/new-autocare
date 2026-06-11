package com.autocare.diagnostics_service.controller;

import com.autocare.diagnostics_service.dto.DiagnosticoRequestDTO;
import com.autocare.diagnostics_service.model.ReporteDiagnostico;
import com.autocare.diagnostics_service.service.DiagnosticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosticos")
@RequiredArgsConstructor
public class DiagnosticoController {

    private final DiagnosticoService service;

    @PostMapping
    public ResponseEntity<ReporteDiagnostico> crearReporte(@Valid @RequestBody DiagnosticoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarDiagnostico(dto));
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<ReporteDiagnostico>> historialVehiculo(@PathVariable Long vehiculoId) {
        return ResponseEntity.ok(service.obtenerHistorialPorVehiculo(vehiculoId));
    }
}