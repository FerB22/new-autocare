package com.autocare.billing_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FacturaRequestDTO(
        @NotNull(message = "El ID de la orden de trabajo es obligatorio") 
        Long ordenTrabajoId,

        @NotNull(message = "El subtotal es obligatorio") 
        @Min(value = 0, message = "El subtotal no puede ser negativo") 
        BigDecimal subtotal,

        @NotNull(message = "Los impuestos son obligatorios") 
        @Min(value = 0, message = "Los impuestos no pueden ser negativos") 
        BigDecimal impuestos
) {}