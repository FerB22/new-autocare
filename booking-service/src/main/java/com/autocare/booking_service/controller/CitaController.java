package com.autocare.booking_service.controller;

import com.autocare.booking_service.dto.CitaRequestDTO;
import com.autocare.booking_service.model.Cita;
import com.autocare.booking_service.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas") // Actualizado a la ruta del Gateway
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping
    public ResponseEntity<List<Cita>> listar() {
        return ResponseEntity.ok(citaService.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<Cita> crear(@Valid @RequestBody CitaRequestDTO citaRequest) {
        // Toda la lógica de mapeo ahora se delega limpiamente al servicio
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.agendarCita(citaRequest));
    }
    
    // Aquí puedes agregar más adelante los endpoints con @PathVariable Long id
}