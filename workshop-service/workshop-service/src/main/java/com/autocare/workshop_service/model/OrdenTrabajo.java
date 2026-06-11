package com.autocare.workshop_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_trabajo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long vehiculoId; // Referencia al garage-service

    @Column(nullable = false)
    private Long mecanicoId; // Referencia al hr-service

    @Column(nullable = false)
    private String descripcionFalla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    private LocalDateTime fechaIngreso;
    
    private LocalDateTime fechaCompletada;

    public enum EstadoOrden {
        COTIZACION,
        RECEPCIONADO,
        EN_PROGRESO,
        ESPERANDO_REPUESTOS,
        LISTO
    }
}