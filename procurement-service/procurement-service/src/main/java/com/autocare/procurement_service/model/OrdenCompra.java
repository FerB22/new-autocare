package com.autocare.procurement_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(nullable = false)
    private Long repuestoId; // El SKU numérico del inventory-service

    @Column(nullable = false)
    private Integer cantidadSolicitada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    public enum EstadoOrden {
        SOLICITADA, EN_TRANSITO, RECIBIDA, CANCELADA
    }
}