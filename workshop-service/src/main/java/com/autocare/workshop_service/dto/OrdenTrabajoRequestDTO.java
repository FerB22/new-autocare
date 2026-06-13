package com.autocare.workshop_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrdenTrabajoRequestDTO(
    @NotNull(message = "El ID del vehículo es obligatorio") Long vehiculoId,
    @NotNull(message = "El ID del mecánico es obligatorio") Long mecanicoId,
    @NotBlank(message = "La descripción de la falla es obligatoria") String descripcionFalla
) {}