package com.autocare.analytics_service.controller;

import com.autocare.analytics_service.model.ReporteMensual;
import com.autocare.analytics_service.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AutoCare — Módulo de Analítica y Reportes", description = "API para la generación de reportes mensuales y análisis de datos del desempeño del taller.")
@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @Operation(summary = "Generar reporte mensual", description = "Calcula, almacena y retorna los indicadores de rendimiento del taller correspondientes a un mes y año específicos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte generado y guardado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros de mes o año inválidos o error en el cálculo de métricas")
    })
    @PostMapping("/generar")
    public ResponseEntity<ReporteMensual> forjarReporteMensual(
            @RequestParam Integer mes, 
            @RequestParam Integer anio) {
        return ResponseEntity.ok(service.generarReporte(mes, anio));
    }

    @Operation(summary = "Obtener historial de reportes", description = "Recupera la lista completa de todos los reportes estadísticos mensuales generados históricamente en el taller.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial obtenido de forma exitosa"),
        @ApiResponse(responseCode = "404", description = "No se encontraron reportes registrados en el historial")
    })
    @GetMapping("/historial")
    public ResponseEntity<List<ReporteMensual>> obtenerHistorial() {
        return ResponseEntity.ok(service.listarHistorial());
    }
}