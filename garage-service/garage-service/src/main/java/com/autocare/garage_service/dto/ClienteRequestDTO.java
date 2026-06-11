package com.autocare.garage_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDTO(
        @NotBlank String documentoIdentidad,
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank @Email String email,
        String telefono
) {}