package com.autocare.analytics_service.service;

import com.autocare.analytics_service.client.BillingClient;
import com.autocare.analytics_service.model.ReporteMensual;
import com.autocare.analytics_service.repository.ReporteMensualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ReporteMensualRepository repository;
    private final BillingClient billingClient;

    public ReporteMensual generarReporte(Integer mes, Integer anio) {
        // Evitar duplicar reportes del mismo mes
        Optional<ReporteMensual> reporteExistente = repository.findByMesAndAnio(mes, anio);
        if (reporteExistente.isPresent()) {
            return reporteExistente.get();
        }

        // 1. Recopilar datos de la galaxia (ej: Ingresos desde Billing)
        BigDecimal ingresos = billingClient.obtenerIngresosDelMes(mes, anio);
        
        // 2. Aquí llamarías a un WorkshopClient para saber cuántos autos se repararon
        Integer ordenesCompletadas = 45; // Dato de prueba

        // 3. Ensamblar la fotografía
        ReporteMensual nuevoReporte = new ReporteMensual(
                null, mes, anio, ingresos, ordenesCompletadas, LocalDateTime.now()
        );

        return repository.save(nuevoReporte);
    }

    public List<ReporteMensual> listarHistorial() {
        return repository.findAll();
    }
}