package com.autocare.billing_service.controller;

import com.autocare.billing_service.dto.FacturaRequestDTO;
import com.autocare.billing_service.model.Factura;
import com.autocare.billing_service.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Facturación", description = "Gestión de facturas y cobros del taller mecánico")
@RestController
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @Operation(
        summary = "Listar todas las facturas",
        description = "Retorna una lista con todas las facturas registradas en el sistema, sin importar su estado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de facturas obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Factura>> listar() {
        return ResponseEntity.ok(facturaService.listarTodas());
    }

    @Operation(
        summary = "Obtener factura por ID",
        description = "Busca y retorna una factura específica según su identificador único. Retorna 404 si no existe."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Factura encontrado y retornada exitosamente"),
        @ApiResponse(responseCode = "404", description = "No existe una factura con el ID proporcionado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable Long id) {
        Optional<Factura> resultado = facturaService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Factura no encontrada con ID: " + id));
    }

    @Operation(
        summary = "Buscar facturas por estado de pago",
        description = "Filtra y retorna todas las facturas que tengan el estado de pago indicado (ej: PENDIENTE, PAGADA, ANULADA)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Facturas filtradas por estado retornadas exitosamente"),
        @ApiResponse(responseCode = "400", description = "El valor del estado proporcionado no es válido")
    })
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Object> buscarPorEstado(@PathVariable Factura.EstadoPago estado) {
        return ResponseEntity.ok(facturaService.buscarPorEstado(estado));
    }

    @Operation(
        summary = "Generar una nueva factura",
        description = "Crea una nueva factura a partir de los datos del request. Calcula el total automáticamente y la persiste en base de datos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Factura creada y persistida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al generar la factura")
    })
    @PostMapping
    public ResponseEntity<Object> generar(@Valid @RequestBody FacturaRequestDTO facturaRequest) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.generar(facturaRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Procesar el pago de una factura",
        description = "Cambia el estado de una factura a PAGADA. Solo aplica si la factura está en estado PENDIENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pago procesado y estado actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "La factura no se puede pagar (ya pagada, anulada, o no existe)"),
        @ApiResponse(responseCode = "404", description = "No existe una factura con el ID proporcionado")
    })
    @PatchMapping("/{id}/pagar")
    public ResponseEntity<Object> pagarFactura(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.pagarFactura(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Anular una factura",
        description = "Cambia el estado de una factura a ANULADA. Una factura anulada no puede revertirse ni pagarse."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Factura anulada exitosamente"),
        @ApiResponse(responseCode = "400", description = "La factura no se puede anular (ya anulada, ya pagada, o no existe)"),
        @ApiResponse(responseCode = "404", description = "No existe una factura con el ID proporcionado")
    })
    @PatchMapping("/{id}/anular")
    public ResponseEntity<Object> anularFactura(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.anularFactura(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Eliminar una factura",
        description = "Elimina permanentemente una factura del sistema por su ID. Esta operación no se puede deshacer."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Factura eliminada exitosamente, sin contenido en la respuesta"),
        @ApiResponse(responseCode = "400", description = "No se puede eliminar la factura (regla de negocio o no existe)"),
        @ApiResponse(responseCode = "404", description = "No existe una factura con el ID proporcionado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable Long id) {
        try {
            facturaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}