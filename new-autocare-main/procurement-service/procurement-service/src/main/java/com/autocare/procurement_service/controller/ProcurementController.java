package com.autocare.procurement_service.controller;

import com.autocare.procurement_service.dto.OrdenCompraRequestDTO;
import com.autocare.procurement_service.dto.ProveedorRequestDTO;
import com.autocare.procurement_service.model.OrdenCompra;
import com.autocare.procurement_service.service.ProcurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Compras", description = "Gestión de proveedores y órdenes de compra de repuestos para el taller")
@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class ProcurementController {

    private final ProcurementService service;

    @Operation(
        summary = "Registrar un nuevo proveedor",
        description = "Crea y persiste un nuevo proveedor en el sistema con su RUT, razón social, email de contacto y teléfono."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Proveedor registrado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al registrar el proveedor")
    })
    @PostMapping("/proveedores")
    public ResponseEntity<Object> crearProveedor(@Valid @RequestBody ProveedorRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarProveedor(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Emitir una orden de compra",
        description = "Crea una nueva orden de compra asociada a un proveedor existente. El estado inicial es SOLICITADA y la fecha se asigna automáticamente. Falla si el proveedor no existe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden de compra emitida y persistida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al emitir la orden"),
        @ApiResponse(responseCode = "404", description = "No existe un proveedor con el ID proporcionado")
    })
    @PostMapping("/ordenes")
    public ResponseEntity<Object> emitirOrden(@Valid @RequestBody OrdenCompraRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.emitirOrdenCompra(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Marcar una orden de compra como recibida",
        description = "Actualiza el estado de una orden de compra a RECIBIDA. Falla si la orden ya fue marcada como recibida anteriormente o si no existe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden de compra marcada como recibida exitosamente"),
        @ApiResponse(responseCode = "400", description = "La orden ya fue marcada como recibida anteriormente"),
        @ApiResponse(responseCode = "404", description = "No existe una orden de compra con el ID proporcionado")
    })
    @PatchMapping("/ordenes/{id}/recibir")
    public ResponseEntity<Object> recibirOrden(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.marcarComoRecibida(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}