package com.autocare.garage_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehiculoRequestDTO(
        @NotBlank String patente,
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotNull Integer anio,
        String color,
        @Size(max = 17, message = "El VIN no puede tener más de 17 caracteres") 
        String vin
) {}