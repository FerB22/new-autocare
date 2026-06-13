package com.autocare.loyalty_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearPerfilDTO(
        @NotNull(message = "El ID del cliente es obligatorio")
        @Positive(message = "El ID del cliente debe ser un número positivo")
        Long clienteId
) {}