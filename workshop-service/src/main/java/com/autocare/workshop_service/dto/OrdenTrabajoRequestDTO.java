package com.autocare.workshop_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrdenTrabajoRequestDTO(
        @NotNull Long vehiculoId,
        @NotNull Long mecanicoId,
        @NotBlank String descripcionFalla
) {}