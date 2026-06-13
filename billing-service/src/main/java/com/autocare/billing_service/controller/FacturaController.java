package com.autocare.billing_service.controller;

import com.autocare.billing_service.dto.FacturaRequestDTO;
import com.autocare.billing_service.model.Factura;
import com.autocare.billing_service.service.FacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "🧾 Módulo de Facturación", description = "Servicios REST para el procesamiento de cobros, control tributario y estados de pago de reparaciones.")
@RestController
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping
    @Operation(
        summary = "Listar todas las facturas", 
        description = "Recupera el historial completo de documentos fiscales y facturas emitidas por el taller mecánico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de facturas obtenido exitosamente.")
    })
    public ResponseEntity<List<Factura>> listar() {
        return ResponseEntity.ok(facturaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener factura por ID", 
        description = "Busca una factura específica utilizando su identificador único numérico para revisar su desglose monetario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Factura localizada y devuelta con éxito."),
        @ApiResponse(responseCode = "404", description = "No existe un registro fiscal asociado al ID proporcionado.")
    })
    public ResponseEntity<Object> buscarPorId(
            @Parameter(description = "Identificador de la factura (Long)", required = true) 
            @PathVariable Long id) {
        Optional<Factura> resultado = facturaService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Factura no encontrada con ID: " + id));
    }

    @GetMapping("/estado/{estado}")
    @Operation(
        summary = "Filtrar facturas por estado de pago", 
        description = "Retorna un conjunto de documentos que coincidan con un criterio específico (ej: PENDIENTE, PAGADA, ANULADA)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Colección de facturas filtradas devuelta con éxito."),
        @ApiResponse(responseCode = "400", description = "El estado de pago proporcionado no es válido para el sistema.")
    })
    public ResponseEntity<Object> buscarPorEstado(
            @Parameter(description = "Estado de pago de la factura (PENDIENTE, PAGADA, ANULADA)", required = true) 
            @PathVariable Factura.EstadoPago estado) {
        return ResponseEntity.ok(facturaService.buscarPorEstado(estado));
    }

    @PostMapping
    @Operation(
        summary = "Generar una nueva factura tributaria", 
        description = "Consolida las horas de mano de obra y repuestos utilizados. Por regla de negocio (RN-11), la orden de trabajo origen debe estar FINALIZADA obligatoriamente para proceder con la emisión."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Documento fiscal generado y registrado correctamente."),
        @ApiResponse(responseCode = "400", description = "Error de negocio: Parámetros inválidos o la Orden de Trabajo sigue abierta en taller.")
    })
    public ResponseEntity<Object> generar(@Valid @RequestBody FacturaRequestDTO facturaRequest) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.generar(facturaRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/pagar")
    @Operation(
        summary = "Procesar el pago de una factura", 
        description = "Cambia el estado del documento fiscal a PAGADA. Esto ejecuta en cascada reglas de fidelización para acumular puntos al perfil del cliente (RN-12)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transacción financiera completada. Factura marcada como PAGADA."),
        @ApiResponse(responseCode = "400", description = "El pago no pudo procesarse (ej: la factura ya estaba pagada o anulada).")
    })
    public ResponseEntity<Object> pagarFactura(
            @Parameter(description = "ID de la factura que se desea pagar", required = true) 
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.pagarFactura(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/anular")
    @Operation(
        summary = "Anular una factura emitida", 
        description = "Cambia el estado del documento a ANULADA por errores en la digitación o reclamos del servicio. Libera las restricciones financieras previas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documento anulado correctamente de los registros."),
        @ApiResponse(responseCode = "400", description = "No se puede anular la factura debido a su estado actual.")
    })
    public ResponseEntity<Object> anularFactura(
            @Parameter(description = "ID de la factura que se desea anular", required = true) 
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(facturaService.anularFactura(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar físicamente una factura", 
        description = "Remueve permanentemente el registro de la base de datos de facturación. Operación restringida para auditorías técnicas."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "24", description = "Factura eliminada sin dejar remanentes de datos de manera física (No Content)."),
        @ApiResponse(responseCode = "400", description = "Fallo de eliminación física por integridad referencial.")
    })
    public ResponseEntity<Object> eliminar(
            @Parameter(description = "ID de la factura a eliminar de los registros", required = true) 
            @PathVariable Long id) {
        try {
            facturaService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}