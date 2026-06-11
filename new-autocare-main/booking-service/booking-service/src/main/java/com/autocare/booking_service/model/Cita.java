package com.autocare.booking_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Cambiado de String a Long para mantener cohesión

    @Column(nullable = false)
    private Long vehiculoId; // Alineado con garage-service

    @Column(nullable = false)
    private Long clienteId; // Alineado con garage-service

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado = EstadoCita.AGENDADA;

    public enum EstadoCita {
        AGENDADA, CONFIRMADA, CANCELADA, EJECUTADA
    }
}