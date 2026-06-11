package com.autocare.customer_service.controller;

import com.autocare.customer_service.model.Cliente;
import com.autocare.customer_service.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.autocare.customer_service.dto.ClienteRequestDTO;
import com.autocare.customer_service.dto.ClienteResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST que expone los endpoints para la gestión de clientes.
 * @RestController Indica a Spring que esta clase procesará peticiones web y
 * serializará automáticamente las respuestas en el cuerpo (body) en formato JSON.
 * @RequestMapping("/clientes") Define la ruta base para todos los métodos HTTP de esta clase.
 */
@Tag(name = "Clientes", description = "Gestión de clientes del taller") // Documentación interactiva de Swagger/OpenAPI
@RestController
@RequestMapping("/clientes")
public class ClienteController {

    // Dependencia del servicio para manejar la lógica de negocio de los clientes.
    // Declarada como 'final' para garantizar su inmutabilidad una vez inyectada.
    private final ClienteService clienteService;

    /**
     * Inyección de dependencias a través del constructor.
     * Esta es la práctica recomendada en Spring Boot en lugar de usar @Autowired,
     * ya que facilita el testing y asegura que la dependencia no sea nula.
     */
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /**
     * Endpoint GET para listar todos los clientes registrados.
     * @return Respuesta HTTP 200 (OK) con la lista completa.
     */
    @Operation(summary = "Listar todos los clientes")
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        // Obtiene todos los clientes desde el servicio de negocio.
        // El servicio devuelve entidades Cliente que luego se transforman
        // en objetos de respuesta (DTO) adecuados para la API.
        List<ClienteResponseDTO> lista = clienteService.listarTodos()
            .stream()
            .map(this::mapearEntidadAResponseDto)
            .toList();

        // Devuelve la lista convertida con código HTTP 200 (OK).
        return ResponseEntity.ok(lista);
    }

    /**
     * Endpoint GET para buscar un cliente específico por su identificador único.
     * @PathVariable vincula el valor "{id}" de la URL al parámetro del método.
     */
    @Operation(summary = "Obtener cliente por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Object> buscarPorId(@PathVariable String id) {
        // Busca el cliente en la base de datos por su ID único
        Optional<Cliente> resultado = clienteService.buscarPorId(id);
        
        // Verifica si el cliente existe en los resultados
        if (resultado.isPresent()) {
            // Si existe, devuelve HTTP 200 (OK) con los datos del cliente mapeados a DTO
            return ResponseEntity.ok(mapearEntidadAResponseDto(resultado.get()));
        }
        
        // Si no existe, devuelve HTTP 404 (NOT_FOUND) con un mensaje de error
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Cliente no encontrado con ID: " + id));
    }

    /**
     * Endpoint GET adicional para buscar a un cliente mediante su correo electrónico.
     * Útil para validaciones o búsquedas donde no se conoce el ID interno.
     */
    @Operation(summary = "Buscar cliente por email")
    @GetMapping("/email/{email}")
    public ResponseEntity<Object> buscarPorEmail(@PathVariable String email) {
        Optional<Cliente> resultado = clienteService.buscarPorEmail(email);
        // Llama al servicio para buscar un cliente por su email.
        // El servicio devuelve un Optional<Cliente> porque el cliente puede no existir.
        if (resultado.isPresent()) {
            // Si el Optional contiene un Cliente, lo mapeamos a DTO y devolvemos HTTP 200 (OK).
            return ResponseEntity.ok(mapearEntidadAResponseDto(resultado.get()));
        }
        // Si no se encuentra, devolvemos HTTP 404 (NOT_FOUND) con un cuerpo que
        // contiene un mensaje de error en formato clave/valor.
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Cliente no encontrado con email: " + email));
    }

/**
     * Endpoint POST para registrar un nuevo cliente en el sistema.
     * @Valid Ejecuta las validaciones de Jakarta (ej. @NotBlank, @Email) sobre el DTO/Modelo.
     * @RequestBody Convierte el JSON entrante de la petición HTTP al objeto Java 'Cliente'.
     */
    @Operation(summary = "Crear un nuevo cliente")
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO clienteRequest) {
        // Convierte el DTO recibido en la petición a la entidad de dominio Cliente.
        Cliente cliente = mapearDtoAEntidad(clienteRequest);
        // Persiste el cliente usando el servicio y obtiene la entidad guardada.
        Cliente clienteGuardado = clienteService.guardar(cliente);
        // Devuelve la respuesta con código 201 CREATED y el cliente guardado convertido a DTO.
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapearEntidadAResponseDto(clienteGuardado));
    }

    /**
     * Endpoint PUT para actualizar la información de un cliente existente.
     * La convención REST del método PUT implica que se actualiza (o reemplaza)
     * el recurso completo en base al ID proporcionado.
     */
    @Operation(summary = "Actualizar datos de un cliente")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(@PathVariable String id,
                                                        @Valid @RequestBody ClienteRequestDTO clienteRequest) {
        // Convierte el DTO recibido en la petición a la entidad de dominio
        // Esto asegura que solo los campos permitidos en la API se copien
        // a la entidad antes de enviarla al servicio de persistencia.
        Cliente cliente = mapearDtoAEntidad(clienteRequest);

        // Llama al servicio para actualizar el cliente identificado por 'id'.
        // El servicio se encarga de validar la existencia y aplicar los
        // cambios en la base de datos (o lanzar una excepción si corresponde).
        Cliente clienteActualizado = clienteService.actualizar(id, cliente);

        // Mapea la entidad resultante a un DTO de respuesta y devuelve
        // HTTP 200 (OK) con el cliente actualizado en el cuerpo.
        return ResponseEntity.ok(mapearEntidadAResponseDto(clienteActualizado));
    }

    /**
     * Endpoint DELETE para eliminar (física o lógicamente) a un cliente de la base de datos.
     */
    @Operation(summary = "Eliminar cliente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        // Delega la eliminación y la validación de existencia al Service.
        clienteService.eliminar(id);
        
        // HTTP 204 (No Content) indica que la operación fue un éxito, 
        // pero que el servidor no devolverá ningún contenido en el body.
        return ResponseEntity.noContent().build();
    }

    private Cliente mapearDtoAEntidad(ClienteRequestDTO dto) {
        // Crea una nueva instancia de Cliente y copia los campos desde el DTO
        // El DTO (ClienteRequestDTO) proviene de la petición HTTP y contiene
        // únicamente los datos necesarios para crear/actualizar la entidad.
        Cliente cliente = new Cliente();

        // Mapea campo por campo desde el DTO hacia la entidad
        cliente.setNombre(dto.getNombre());     // Nombre del cliente
        cliente.setApellido(dto.getApellido()); // Apellido del cliente
        cliente.setEmail(dto.getEmail());       // Correo electrónico
        cliente.setTelefono(dto.getTelefono()); // Teléfono de contacto
        cliente.setDireccion(dto.getDireccion()); // Dirección física

        // Retorna la entidad lista para ser usada por el servicio (persistencia/actualización)
        return cliente;
    }

    private ClienteResponseDTO mapearEntidadAResponseDto(Cliente cliente) {
        // Crea una nueva instancia del DTO de respuesta que se enviará al cliente
        ClienteResponseDTO dto = new ClienteResponseDTO();

        // Mapea el identificador único de la entidad al DTO
        dto.setIdCliente(cliente.getIdCliente());

        // Mapea los datos personales básicos del cliente
        dto.setNombre(cliente.getNombre());      // Nombre propio
        dto.setApellido(cliente.getApellido());  // Apellido o apellidos

        // Información de contacto
        dto.setEmail(cliente.getEmail());        // Correo electrónico
        dto.setTelefono(cliente.getTelefono());  // Teléfono de contacto

        // Dirección física del cliente (si está disponible)
        dto.setDireccion(cliente.getDireccion());

        // Devuelve el DTO completamente poblado
        return dto;
    }
}