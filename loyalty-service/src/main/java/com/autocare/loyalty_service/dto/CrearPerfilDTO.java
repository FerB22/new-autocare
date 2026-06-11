package com.autocare.loyalty_service.dto;

import jakarta.validation.constraints.NotNull;

public record CrearPerfilDTO(
        @NotNull Long clienteId
) {}