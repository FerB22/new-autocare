package com.autocare.inventory_service.controller;

import com.autocare.inventory_service.dto.RepuestoRequestDTO;
import com.autocare.inventory_service.dto.StockReductionDTO;
import com.autocare.inventory_service.model.Repuesto;
import com.autocare.inventory_service.service.RepuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Inventario", description = "Gestión de repuestos y control de stock del taller mecánico")
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService service;

    @Operation(
        summary = "Listar todos los repuestos",
        description = "Retorna una lista con todos los repuestos registrados en el inventario, incluyendo su stock actual y mínimo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de repuestos obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Repuesto>> listarInventario() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @Operation(
        summary = "Registrar un nuevo repuesto",
        description = "Crea y persiste un nuevo repuesto en el inventario con su código SKU, precio unitario, stock actual y stock mínimo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Repuesto registrado en el inventario exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al registrar el repuesto")
    })
    @PostMapping
    public ResponseEntity<Object> registrarRepuesto(@Valid @RequestBody RepuestoRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.crearRepuesto(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Reducir stock de un repuesto",
        description = "Descuenta la cantidad utilizada del stock actual de un repuesto. Falla si el stock disponible es menor a la cantidad solicitada o si el repuesto no existe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock reducido y actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente para la cantidad solicitada"),
        @ApiResponse(responseCode = "404", description = "No existe un repuesto con el ID proporcionado")
    })
    @PostMapping("/reducir-stock")
    public ResponseEntity<Object> procesarUsoDeRepuesto(@Valid @RequestBody StockReductionDTO dto) {
        try {
            service.reducirStock(dto);
            return ResponseEntity.ok(Map.of("mensaje", "Stock actualizado correctamente en la base de datos"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}