package com.autocare.procurement_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProveedorRequestDTO(
    @NotBlank(message = "El RUT es obligatorio") String rut,
    @NotBlank(message = "La razón social es obligatoria") String razonSocial,
    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El correo no tiene un formato válido") String emailContacto,
    String telefono
) {}