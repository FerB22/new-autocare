package com.autocare.hr_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mecanicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mecanico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Evolucionado a numérico para enlazarse con workshop-service

    @Column(nullable = false, unique = true)
    private String documentoIdentidad;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String especialidad; // Ej: Electromecánico, Chapista, Diagnóstico

    private String telefono;
    
    @Column(nullable = false)
    private boolean estaDisponible; //
}