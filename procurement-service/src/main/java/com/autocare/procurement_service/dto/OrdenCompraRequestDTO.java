package com.autocare.procurement_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrdenCompraRequestDTO(
    @NotNull(message = "El ID del proveedor es obligatorio") Long proveedorId,
    @NotNull(message = "El ID del repuesto es obligatorio") Long repuestoId,
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima por orden es 1") Integer cantidadSolicitada
) {}