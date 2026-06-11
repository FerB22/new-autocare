package com.autocare.procurement_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrdenCompraRequestDTO(
        @NotNull Long proveedorId,
        @NotNull Long repuestoId,
        @NotNull @Min(1) Integer cantidadSolicitada
) {}