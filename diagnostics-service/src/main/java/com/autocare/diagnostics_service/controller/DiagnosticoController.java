package com.autocare.diagnostics_service.controller;

import com.autocare.diagnostics_service.dto.DiagnosticoRequestDTO;
import com.autocare.diagnostics_service.model.ReporteDiagnostico;
import com.autocare.diagnostics_service.service.DiagnosticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosticos")
@RequiredArgsConstructor
@Tag(
    name = "🔧 Módulo de Diagnósticos",
    description = "Servicios REST para registrar diagnósticos de vehículos y consultar su historial técnico."
)
public class DiagnosticoController {

    private final DiagnosticoService service;

    @PostMapping
    @Operation(
        summary = "Registrar un diagnóstico vehicular",
        description = "Guarda un reporte de diagnóstico validando el DTO de entrada antes de persistirlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Diagnóstico registrado con éxito."),
        @ApiResponse(responseCode = "400", description = "La solicitud tiene datos inválidos o incompletos.")
    })
    public ResponseEntity<ReporteDiagnostico> crearReporte(@Valid @RequestBody DiagnosticoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarDiagnostico(dto));
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    @Operation(
        summary = "Obtener historial de diagnósticos por vehículo",
        description = "Retorna todos los reportes asociados a un vehículo específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial encontrado correctamente."),
        @ApiResponse(responseCode = "404", description = "No existe historial para el vehículo indicado.")
    })
    public ResponseEntity<List<ReporteDiagnostico>> historialVehiculo(
            @Parameter(description = "ID del vehículo", required = true)
            @PathVariable Long vehiculoId) {
        return ResponseEntity.ok(service.obtenerHistorialPorVehiculo(vehiculoId));
    }
}