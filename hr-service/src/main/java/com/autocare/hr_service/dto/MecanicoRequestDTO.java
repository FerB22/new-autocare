package com.autocare.hr_service.dto;

import jakarta.validation.constraints.NotBlank;

public record MecanicoRequestDTO(
        @NotBlank String documentoIdentidad,
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank String especialidad,
        String telefono
) {}