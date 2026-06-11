package com.autocare.loyalty_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransaccionPuntosDTO(
        @NotNull @Min(1) Integer cantidadPuntos
) {}