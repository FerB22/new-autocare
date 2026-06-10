package com.autocare.procurement_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String rut; // Identificador comercial de la empresa

    @Column(nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String emailContacto;

    private String telefono;
}