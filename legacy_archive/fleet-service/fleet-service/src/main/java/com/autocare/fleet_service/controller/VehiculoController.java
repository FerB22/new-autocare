package com.autocare.fleet_service.controller;

import com.autocare.fleet_service.dto.ClienteDTO;
import com.autocare.fleet_service.model.Vehiculo;
import com.autocare.fleet_service.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.autocare.fleet_service.dto.VehiculoRequestDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para el microservicio de flota (fleet-service).
 * Gestiona los puntos de entrada HTTP para la administración de vehículos.
 * @RestController asegura que los retornos se serialicen como JSON.
 */
@Tag(name = "Vehículos", description = "Gestión de vehículos del taller") // Documentación OpenAPI/Swagger
@RestController
@RequestMapping("/vehiculos")
public class VehiculoController {

    // Se inyecta la capa de servicio, manteniendo la lógica de negocio 
    // separada de la lógica de enrutamiento web.
    private final VehiculoService vehiculoService;

    /**
     * Inyección por constructor.
     * Facilita el testing unitario y asegura que el controlador no se inicie 
     * en un estado inválido (sin su servicio).
     */
    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    // GET /vehiculos → lista todos
    /**
     * Endpoint GET para recuperar la colección completa de vehículos.
     * Retorna HTTP 200 (OK).
     */
    @Operation(summary = "Listar todos los vehículos")
    @GetMapping
    public ResponseEntity<List<Vehiculo>> listar() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    // GET /vehiculos/patente/ABC123
    /**
     * Endpoint GET para buscar por una clave de negocio única (la patente).
     * @PathVariable extrae la patente directamente de la URI.
     */
    @GetMapping("/patente/{patente}")
    public ResponseEntity<Object> buscarPorPatente(@PathVariable String patente) {
        Optional<Vehiculo> resultado = vehiculoService.buscarPorPatente(patente);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        // Devuelve HTTP 404 estructurado en JSON si la patente no existe.
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Vehículo no encontrado con patente: " + patente));
    }

    // GET /vehiculos/{id}
    /**
     * Endpoint GET estándar para obtener un recurso por su Clave Primaria (UUID).
     */
    @Operation(summary = "Obtener vehículo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        Optional<Vehiculo> resultado = vehiculoService.buscarPorId(id);
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Vehículo no encontrado con ID: " + id));
    }

    // GET /vehiculos/{id}/duenio → consulta el dueño en customer-service
    /**
     * Patrón de Sub-recurso y Composición de API en Microservicios.
     * Este endpoint permite a los clientes consultar la información del dueño 
     * navegando a través de la relación del vehículo.
     * * Funciona como un orquestador ligero: primero valida el recurso local, 
     * y luego delega la consulta remota al customer-service.
     */
    @Operation(summary = "Obtener el dueño de un vehículo")
    @GetMapping("/{id}/duenio")
    public ResponseEntity<ClienteDTO> obtenerDuenio(@PathVariable String id) {
        // Primero buscamos el vehículo para obtener el idDuenio localmente.
        // Si no existe, lanzamos un 404 directo en lugar de ensuciar con try-catch.
        Vehiculo vehiculo = vehiculoService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehículo no encontrado con ID: " + id));

        // Se extrae la referencia externa (foreign key lógica).
        String idDuenio = vehiculo.getIdDuenio();
        
        // El servicio realiza una llamada HTTP síncrona y mapea el JSON a un DTO.
        // Resiliencia: Si el customer-service está caído, la captura de la falla
        // y la respuesta HTTP 503 ahora suceden automáticamente dentro de VehiculoService.
        ClienteDTO cliente = vehiculoService.obtenerDuenio(idDuenio);
        
        return ResponseEntity.ok(cliente); // HTTP 200 OK
    }

    // POST /vehiculos → crea nuevo
    /**
     * Endpoint POST para insertar un nuevo vehículo.
     * @Valid obliga a pasar por las restricciones de Jakarta (@NotBlank, Regex, etc.).
     * 
     * Flujo:
     * 1. Recibe el DTO con los datos del vehículo desde el cliente.
     * 2. Mapea el DTO a la entidad Vehiculo mediante mapearDtoAEntidad().
     * 3. Persiste el vehículo llamando a vehiculoService.guardar().
     * 4. Retorna HTTP 201 (CREATED) con la entidad guardada en el body.
     */
    @Operation(summary = "Crear un nuevo vehículo")
    @PostMapping
    public ResponseEntity<Vehiculo> crear(@Valid @RequestBody VehiculoRequestDTO vehiculoRequest) {
        // Transforma el DTO de entrada a entidad de dominio (desacoplamiento).
        Vehiculo vehiculo = mapearDtoAEntidad(vehiculoRequest);
        
        // Delega la persistencia al servicio y retorna con código 201 (CREATED).
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.guardar(vehiculo));
    }

    // PUT /vehiculos/{id} → actualiza
    /**
     * Endpoint PUT para la actualización o reemplazo del recurso.
     * Recibe un ID de vehículo y los nuevos datos, actualiza la entidad completa.
     * 
     * Flujo:
     * 1. Extrae el ID del vehículo a actualizar desde la URI.
     * 2. Valida el DTO de entrada mediante @Valid.
     * 3. Transforma el DTO a entidad de dominio.
     * 4. Delega la actualización al servicio.
     * 5. Retorna HTTP 200 (OK) con la entidad actualizada.
     */
    @Operation(summary = "Actualizar datos de un vehículo")
    @PutMapping("/{id}")
    public ResponseEntity<Vehiculo> actualizar(@PathVariable String id,
                                            @Valid @RequestBody VehiculoRequestDTO vehiculoRequest) {
        // Mapea el DTO de entrada a la entidad Vehiculo (desacoplamiento).
        Vehiculo vehiculo = mapearDtoAEntidad(vehiculoRequest);
        
        // Persiste los cambios llamando al servicio y retorna con código 200 (OK).
        return ResponseEntity.ok(vehiculoService.actualizar(id, vehiculo));
    }

    // DELETE /vehiculos/{id} → elimina
    /**
     * Endpoint DELETE para el borrado del recurso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        // Delega la eliminación y la validación de existencia al Service.
        vehiculoService.eliminar(id);
        
        // Retorna HTTP 204 (No Content) estándar para eliminaciones exitosas.
        return ResponseEntity.noContent().build();
    }

    /**
     * Transforma un DTO (Data Transfer Object) de entrada a la entidad de dominio.
     * Desacopla la representación JSON recibida del cliente de la entidad interna.
     * @param dto VehiculoRequestDTO con los datos del cliente.
     * @return Entidad Vehiculo mapeada y lista para persistencia.
     */
    private Vehiculo mapearDtoAEntidad(VehiculoRequestDTO dto) {
        // Instancia una nueva entidad vacía.
        Vehiculo vehiculo = new Vehiculo();
        
        // Asigna cada campo del DTO a la entidad correspondiente.
        vehiculo.setPatente(dto.getPatente());
        vehiculo.setMarca(dto.getMarca());
        vehiculo.setModelo(dto.getModelo());
        vehiculo.setAnio(dto.getAnio());
        vehiculo.setVinChasis(dto.getVinChasis());
        // Referencia externa al cliente propietario en el microservicio de clientes.
        vehiculo.setIdDuenio(dto.getIdDuenio());
        
        // Retorna la entidad poblada.
        return vehiculo;
    }
}