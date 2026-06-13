package com.autocare.inventory_service.controller;

import com.autocare.inventory_service.dto.RepuestoRequestDTO;
import com.autocare.inventory_service.dto.StockReductionDTO;
import com.autocare.inventory_service.model.Repuesto;
import com.autocare.inventory_service.service.RepuestoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Tag(name = "📦 Módulo de Inventario", description = "Servicios REST para el control de stock, catálogo de repuestos y validaciones de disponibilidad de insumos.")
public class RepuestoController {

    private final RepuestoService service;

    @GetMapping
    @Operation(
        summary = "Listar el catálogo de inventario completo", 
        description = "Recupera todos los repuestos registrados en el sistema, mostrando sus detalles, precios y niveles de existencias actuales."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catálogo de inventario recuperado exitosamente.")
    })
    public ResponseEntity<List<Repuesto>> listarInventario() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @PostMapping
    @Operation(
        summary = "Registrar un nuevo repuesto en el catálogo", 
        description = "Añade un nuevo artículo o componente vehicular al inventario con su stock inicial y umbral mínimo configurado para alertas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Repuesto creado e indexado correctamente en el sistema."),
        @ApiResponse(responseCode = "400", description = "Estructura del payload inválida o datos faltantes.")
    })
    public ResponseEntity<Repuesto> registrarRepuesto(@Valid @RequestBody RepuestoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearRepuesto(dto));
    }

    @PostMapping("/reducir-stock")
    @Operation(
        summary = "Procesar uso y consumo de repuestos", 
        description = "Descuenta una cantidad específica de existencias físicas. Si el stock desciende por debajo de su límite configurado, dispara una regla de negocio de alerta de reabastecimiento crítico (RN-07 / RN-08)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock actualizado correctamente en la base de datos."),
        @ApiResponse(responseCode = "400", description = "Solicitud rechazada: No hay suficiente stock físico disponible para realizar el descuento.")
    })
    public ResponseEntity<String> procesarUsoDeRepuesto(@Valid @RequestBody StockReductionDTO dto) {
        service.reducirStock(dto);
        return ResponseEntity.ok("Stock actualizado correctamente en la base de datos");
    }
}