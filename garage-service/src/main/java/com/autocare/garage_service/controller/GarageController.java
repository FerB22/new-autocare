package com.autocare.garage_service.controller;

import com.autocare.garage_service.dto.ClienteRequestDTO;
import com.autocare.garage_service.dto.VehiculoRequestDTO;
import com.autocare.garage_service.model.Cliente;
import com.autocare.garage_service.model.Vehiculo;
import com.autocare.garage_service.service.GarageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garage/clientes")
@RequiredArgsConstructor
public class GarageController {

    private final GarageService service;

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(service.obtenerTodosLosClientes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerClientePorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPerfilCompleto(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarCliente(dto));
    }

    // El endpoint anidado que demuestra la relación
    @PostMapping("/{clienteId}/vehiculos")
    public ResponseEntity<Vehiculo> agregarVehiculo(
            @PathVariable Long clienteId,
            @Valid @RequestBody VehiculoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarVehiculoEnGarage(clienteId, dto));
    }
}