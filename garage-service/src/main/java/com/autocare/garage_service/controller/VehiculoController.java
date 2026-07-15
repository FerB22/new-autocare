package com.autocare.garage_service.controller;

import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.service.GarageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/garage/vehiculos")
@RequiredArgsConstructor
@Tag(name = "Módulo Vehículos", description = "Servicios REST auxiliares para la consulta y verificación de vehículos en el garage.")
public class VehiculoController {

    private final GarageService service;

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener detalles de un vehículo por su ID",
        description = "Busca un vehículo en el sistema y retorna su información. Utilizado para la validación entre microservicios."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehículo encontrado exitosamente."),
        @ApiResponse(responseCode = "404", description = "El vehículo con el ID proporcionado no existe en el sistema.")
    })
    public ResponseEntity<Vehiculo> obtenerVehiculoPorId(
            @Parameter(description = "Identificador único del vehículo (Long)", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerVehiculoPorId(id));
    }
}
