package com.autocare.analytics_service.controller;

import com.autocare.analytics_service.model.ReporteMensual;
import com.autocare.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "API para la gestión de métricas y reportes")
public class AnalyticsController {

    private final AnalyticsService service;

    @Operation(summary = "Generar reporte mensual", description = "Calcula y consolida las métricas financieras y operativas de un mes específico.")
    @ApiResponse(responseCode = "200", description = "Reporte generado correctamente")
    @PostMapping("/generar")
    public ResponseEntity<ReporteMensual> forjarReporteMensual(
            @RequestParam Integer mes, 
            @RequestParam Integer anio) {
        return ResponseEntity.ok(service.generarReporte(mes, anio));
    }

    @Operation(summary = "Consultar historial", description = "Obtiene la lista de todos los reportes mensuales generados previamente.")
    @ApiResponse(responseCode = "200", description = "Historial recuperado exitosamente")
    @GetMapping("/historial")
    public ResponseEntity<List<ReporteMensual>> obtenerHistorial() {
        return ResponseEntity.ok(service.listarHistorial());
    }
}