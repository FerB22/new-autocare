package com.autocare.diagnostics_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reportes_diagnostico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDiagnostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehiculoId; // Enlace fantasma al garage-service

    @Column(nullable = false)
    private Long mecanicoId; // Enlace fantasma al hr-service

    @Column(nullable = false)
    private Integer kilometrajeActual;

    @ElementCollection
    @CollectionTable(name = "codigos_obd2", joinColumns = @JoinColumn(name = "reporte_id"))
    @Column(name = "codigo")
    private List<String> codigosScanner; // Ej: ["P0300", "P0420"]

    @Column(length = 1000)
    private String observacionesVisuales; // Ej: "Fuga de aceite en cárter"

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;
}