package com.autocare.workshop_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AsignarRepuestoDTO(
    @NotNull(message = "El ID del repuesto es obligatorio") Long repuestoId,
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima a usar es 1") Integer cantidad
) {}