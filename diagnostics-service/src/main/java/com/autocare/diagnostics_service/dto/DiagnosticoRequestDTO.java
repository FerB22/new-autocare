package com.autocare.diagnostics_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DiagnosticoRequestDTO(
        @NotNull Long vehiculoId,
        @NotNull Long mecanicoId,
        @NotNull @Min(0) Integer kilometrajeActual,
        List<String> codigosScanner,
        String observacionesVisuales
) {}