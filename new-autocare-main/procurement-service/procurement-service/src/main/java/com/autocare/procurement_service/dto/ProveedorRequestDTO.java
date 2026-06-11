package com.autocare.procurement_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProveedorRequestDTO(
        @NotBlank String rut,
        @NotBlank String razonSocial,
        @NotBlank @Email String emailContacto,
        String telefono
) {}