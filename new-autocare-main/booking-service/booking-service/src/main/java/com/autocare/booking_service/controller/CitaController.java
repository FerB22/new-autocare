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
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reservas", description = "Gestión de citas y agendamiento de servicios del taller mecánico")
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @Operation(
        summary = "Listar todas las citas",
        description = "Retorna una lista con todas las citas agendadas en el sistema, sin filtro de estado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de citas obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Cita>> listar() {
        return ResponseEntity.ok(citaService.obtenerTodas());
    }

    @Operation(
        summary = "Agendar una nueva cita",
        description = "Crea y persiste una nueva cita a partir de los datos del request. El estado inicial es AGENDADA automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cita agendada y persistida exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos del request inválidos o error de negocio al agendar la cita")
    })
    @PostMapping
    public ResponseEntity<Object> crear(@Valid @RequestBody CitaRequestDTO citaRequest) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(citaService.agendarCita(citaRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}