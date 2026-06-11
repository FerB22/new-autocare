package com.autocare.analytics_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_mensuales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private BigDecimal ingresosTotales;

    @Column(nullable = false)
    private Integer totalOrdenesCompletadas;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;
}