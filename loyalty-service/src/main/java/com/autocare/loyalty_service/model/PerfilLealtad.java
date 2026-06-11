package com.autocare.loyalty_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "perfiles_lealtad")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilLealtad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long clienteId; // Enlace fantasma al garage-service

    @Column(nullable = false)
    private Integer puntosAcumulados;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelLealtad nivel;

    @Column(nullable = false)
    private LocalDateTime ultimaActualizacion;

    public enum NivelLealtad {
        BRONCE, PLATA, ORO, VIP
    }
}