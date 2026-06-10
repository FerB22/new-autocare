package com.autocare.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockReductionDTO(
        @NotNull Long repuestoId,
        @NotNull @Min(1) Integer cantidadUtilizada
) {}