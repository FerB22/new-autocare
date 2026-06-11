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
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Diagnósticos", description = "Registro y consulta de reportes de diagnóstico técnico de vehículos")
@RestController
@RequestMapping("/api/diagnosticos")
@RequiredArgsConstructor
public class DiagnosticoController {

    private final DiagnosticoService service;

    // ─── POST /api/diagnosticos ──────────────────────────────────────────────
    @Operation(
        summary = "Registrar un nuevo diagnóstico",
        description = "Crea y persiste un reporte de diagnóstico técnico para un vehículo. Incluye códigos de scanner, observaciones visuales y kilometraje actual. La fecha se asigna automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reporte de diagnóstico registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al registrar el diagnóstico")
    })
    @PostMapping
    public ResponseEntity<Object> crearReporte(@Valid @RequestBody DiagnosticoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarDiagnostico(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ─── GET /api/diagnosticos/vehiculo/{vehiculoId} ─────────────────────────
    @Operation(
        summary = "Obtener historial de diagnósticos por vehículo",
        description = "Retorna todos los reportes de diagnóstico registrados para un vehículo específico, identificado por su ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial de diagnósticos obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existen diagnósticos para el vehículo con el ID proporcionado")
    })
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<ReporteDiagnostico>> historialVehiculo(@PathVariable Long vehiculoId) {
        return ResponseEntity.ok(service.obtenerHistorialPorVehiculo(vehiculoId));
    }
}