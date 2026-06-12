package com.autocare.analytics_service.controller;

import com.autocare.analytics_service.model.ReporteMensual;
import com.autocare.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @PostMapping("/generar")
    public ResponseEntity<ReporteMensual> forjarReporteMensual(
            @RequestParam Integer mes, 
            @RequestParam Integer anio) {
        return ResponseEntity.ok(service.generarReporte(mes, anio));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<ReporteMensual>> obtenerHistorial() {
        return ResponseEntity.ok(service.listarHistorial());
    }
}