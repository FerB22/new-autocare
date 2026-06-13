package com.autocare.loyalty_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TransaccionPuntosDTO(
        @NotNull(message = "La cantidad de puntos es obligatoria")
        @Min(value = 1, message = "La cantidad mínima de puntos por transacción es 1")
        Integer cantidadPuntos
) {}