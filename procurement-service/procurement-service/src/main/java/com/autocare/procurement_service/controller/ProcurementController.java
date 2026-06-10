package com.autocare.procurement_service.controller;

import com.autocare.procurement_service.dto.OrdenCompraRequestDTO;
import com.autocare.procurement_service.dto.ProveedorRequestDTO;
import com.autocare.procurement_service.model.OrdenCompra;
import com.autocare.procurement_service.model.Proveedor;
import com.autocare.procurement_service.service.ProcurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class ProcurementController {

    private final ProcurementService service;

    @PostMapping("/proveedores")
    public ResponseEntity<Proveedor> crearProveedor(@Valid @RequestBody ProveedorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarProveedor(dto));
    }

    @PostMapping("/ordenes")
    public ResponseEntity<OrdenCompra> emitirOrden(@Valid @RequestBody OrdenCompraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitirOrdenCompra(dto));
    }

    @PatchMapping("/ordenes/{id}/recibir")
    public ResponseEntity<OrdenCompra> recibirOrden(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarComoRecibida(id));
    }
}