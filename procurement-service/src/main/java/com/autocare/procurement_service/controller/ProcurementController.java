package com.autocare.procurement_service.controller;

import com.autocare.procurement_service.dto.OrdenCompraRequestDTO;
import com.autocare.procurement_service.dto.ProveedorRequestDTO;
import com.autocare.procurement_service.model.OrdenCompra;
import com.autocare.procurement_service.model.Proveedor;
import com.autocare.procurement_service.service.ProcurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
@Tag(
    name = "Módulo de Compras",
    description = "Servicios REST para el registro de proveedores, emisión de órdenes de compra y recepción de mercancía."
)
public class ProcurementController {

    private final ProcurementService service;

    @PostMapping("/proveedores")
    @Operation(
        summary = "Registrar un proveedor",
        description = "Crea un nuevo proveedor validando RUT, razón social y correo de contacto."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Proveedor registrado con éxito."),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o proveedor ya existente.")
    })
    public ResponseEntity<EntityModel<Proveedor>> crearProveedor(@Valid @RequestBody ProveedorRequestDTO dto) {
        Proveedor proveedor = service.registrarProveedor(dto);
        EntityModel<Proveedor> recurso = EntityModel.of(proveedor);

        recurso.add(linkTo(methodOn(ProcurementController.class).crearProveedor(null)).withSelfRel());
        recurso.add(linkTo(methodOn(ProcurementController.class).emitirOrden(null)).withRel("emitir-orden"));

        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    @PostMapping("/ordenes")
    @Operation(
        summary = "Emitir una orden de compra",
        description = "Genera una nueva orden de compra vinculando un proveedor y un repuesto del inventario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Orden de compra emitida con éxito."),
        @ApiResponse(responseCode = "400", description = "Datos inválidos, proveedor o repuesto no encontrado.")
    })
    public ResponseEntity<EntityModel<OrdenCompra>> emitirOrden(@Valid @RequestBody OrdenCompraRequestDTO dto) {
        OrdenCompra orden = service.emitirOrdenCompra(dto);
        EntityModel<OrdenCompra> recurso = EntityModel.of(orden);

        recurso.add(linkTo(methodOn(ProcurementController.class).emitirOrden(null)).withSelfRel());
        recurso.add(linkTo(methodOn(ProcurementController.class).recibirOrden(orden.getId())).withRel("recibir-orden"));

        return ResponseEntity.status(HttpStatus.CREATED).body(recurso);
    }

    @PatchMapping("/ordenes/{id}/recibir")
    @Operation(
        summary = "Marcar orden de compra como recibida",
        description = "Cambia el estado de una orden de SOLICITADA o EN_TRANSITO a RECIBIDA."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orden marcada como recibida correctamente."),
        @ApiResponse(responseCode = "404", description = "No se encontró la orden de compra.")
    })
    public ResponseEntity<EntityModel<OrdenCompra>> recibirOrden(
            @Parameter(description = "ID de la orden de compra", required = true)
            @PathVariable Long id) {
        OrdenCompra orden = service.marcarComoRecibida(id);
        EntityModel<OrdenCompra> recurso = EntityModel.of(orden);

        recurso.add(linkTo(methodOn(ProcurementController.class).recibirOrden(id)).withSelfRel());
        recurso.add(linkTo(methodOn(ProcurementController.class).emitirOrden(null)).withRel("emitir-nueva-orden"));

        return ResponseEntity.ok(recurso);
    }
}