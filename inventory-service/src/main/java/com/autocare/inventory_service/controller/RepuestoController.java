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

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService service;

    @GetMapping
    public ResponseEntity<List<Repuesto>> listarInventario() {
        return ResponseEntity.ok(service.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<Repuesto> registrarRepuesto(@Valid @RequestBody RepuestoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearRepuesto(dto));
    }

    @PostMapping("/reducir-stock")
    public ResponseEntity<String> procesarUsoDeRepuesto(@Valid @RequestBody StockReductionDTO dto) {
        service.reducirStock(dto);
        return ResponseEntity.ok("Stock actualizado correctamente en la base de datos");
    }
}