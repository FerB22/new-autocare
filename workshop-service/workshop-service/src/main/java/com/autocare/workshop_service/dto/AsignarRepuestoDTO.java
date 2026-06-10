package com.autocare.workshop_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AsignarRepuestoDTO(
        @NotNull Long repuestoId,
        @NotNull @Min(1) Integer cantidad
) {}