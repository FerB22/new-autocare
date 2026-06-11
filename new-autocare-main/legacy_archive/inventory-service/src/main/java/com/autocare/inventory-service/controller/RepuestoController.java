package com.autocare.spare_parts_service.controller;

import com.autocare.spare_parts_service.dto.RepuestoRequestDTO;
import com.autocare.spare_parts_service.model.Repuesto;
import com.autocare.spare_parts_service.service.RepuestoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para el microservicio de Repuestos (spare-parts-service).
 * Actúa como el punto de entrada HTTP (API Gateway/Frontend) para gestionar el inventario.
 * @RestController indica que cada método devuelve un objeto que será serializado
 * automáticamente a JSON en el cuerpo de la respuesta HTTP.
 */
@Validated
@Tag(name = "Repuesto", description = "Gestión de repuesto")
@RestController
@RequestMapping("/repuestos")
public class RepuestoController {

    // Dependencia inyectada del servicio. Se declara como 'final' para garantizar
    // que el controlador siempre mantenga su capa lógica y sea inmutable.
    private final RepuestoService repuestoService;

    /**
     * Inyección de dependencias por constructor.
     * Es la práctica recomendada de Spring (sobre el uso de @Autowired en el campo),
     * ya que facilita la escritura de pruebas unitarias inyectando un mock del servicio.
     */
    public RepuestoController(RepuestoService repuestoService) {
        this.repuestoService = repuestoService;
    }

    /**
     * Endpoint GET genérico para obtener todo el catálogo de repuestos.
     * @return HTTP 200 (OK) con la lista completa.
     */
    @Operation(summary = "Listar repuestos")
    @GetMapping
    public ResponseEntity<List<Repuesto>> listar() {
        return ResponseEntity.ok(repuestoService.listarTodos());
    }

    /**
     * Endpoint GET para recuperar un repuesto específico usando su identificador interno (UUID).
     * @PathVariable vincula la parte de la ruta "{id}" al parámetro del método.
     */
    @Operation(summary = "Obtener repuesto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        Optional<Repuesto> resultado = repuestoService.buscarPorId(id);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Repuesto no encontrado con ID: " + id));
    }

    /**
     * Endpoint GET para recuperar un repuesto usando una clave de negocio (su código de parte).
     * Ideal para búsquedas que hacen los mecánicos directamente desde el sistema o lector de código.
     */
    @Operation(summary = "Buscar repuesto por código")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Object> buscarPorCodigo(@PathVariable String codigo) {
        Optional<Repuesto> resultado = repuestoService.buscarPorCodigo(codigo);

        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Repuesto no encontrado con código: " + codigo));
    }

    /**
     * Endpoint GET optimizado para obtener solo los repuestos que tienen disponibilidad.
     * Muy útil para el frontend al llenar selectores desplegables al momento de cotizar.
     */
    @Operation(summary = "Listar repuestos con stock")
    @GetMapping("/con-stock")
    public ResponseEntity<List<Repuesto>> listarConStock() {
        return ResponseEntity.ok(repuestoService.listarConStock());
    }

    /**
     * Endpoint POST para registrar un nuevo repuesto en el inventario.
     * @Valid obliga a ejecutar las reglas de validación de Jakarta Bean Validation
     * definidas en el DTO antes de procesar la petición.
     * @RequestBody transforma el JSON que viene en la petición a un objeto Java.
     */
    @Operation(summary = "Crear nuevo repuesto")
    @PostMapping
    public ResponseEntity<Object> crear(@Valid @RequestBody RepuestoRequestDTO repuestoRequest) {
        try {
            Repuesto repuesto = mapearDtoAEntidad(repuestoRequest);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(repuestoService.guardar(repuesto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint POST que ejecuta una acción de negocio concreta.
     * Permite descontar stock de manera controlada usando un parámetro de cantidad.
     */
    @Operation(summary = "Reservar stock de un repuesto")
    @PostMapping("/{id}/reservar")
    public ResponseEntity<Object> reservar(@PathVariable String id,
                                           @RequestParam @Min(value = 1, message = "La cantidad debe ser mayor a 0") int cantidad) {
        try {
            return ResponseEntity.ok(repuestoService.reservar(id, cantidad));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint PUT diseñado para actualizar la información completa de un repuesto existente.
     * De acuerdo a REST, PUT reemplaza la representación del recurso actual con la del payload.
     */
    @Operation(summary = "Actualizar repuesto")
    @PutMapping("/{id}")
    public ResponseEntity<Object> actualizar(@PathVariable String id,
                                             @Valid @RequestBody RepuestoRequestDTO repuestoRequest) {
        try {
            Repuesto repuesto = mapearDtoAEntidad(repuestoRequest);
            return ResponseEntity.ok(repuestoService.actualizar(id, repuesto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint DELETE para eliminar (dar de baja) un repuesto del sistema.
     */
    @Operation(summary = "Eliminar por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminar(@PathVariable String id) {
        try {
            repuestoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Convierte el DTO recibido desde la API en una entidad Repuesto.
     * Este mapeo manual ayuda a separar claramente la capa web de la capa de persistencia.
     */
    private Repuesto mapearDtoAEntidad(RepuestoRequestDTO dto) {
        Repuesto repuesto = new Repuesto();
        repuesto.setNombre(dto.getNombre());
        repuesto.setCodigoParte(dto.getCodigoParte());
        repuesto.setStock(dto.getStock());
        repuesto.setPrecioUnitario(dto.getPrecioUnitario());
        repuesto.setUbicacionBodega(dto.getUbicacionBodega());
        return repuesto;
    }
}