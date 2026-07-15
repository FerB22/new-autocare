# DEFENSAS TÉCNICAS DE ARQUITECTURA DE MICROSERVICIOS
## EVALUACIÓN PRÁCTICA EN VIVO: GALAXY-REGISTRY-SERVICE
**Docente Evaluador:** Antigravity (Strict but Fair Coding Assistant)
**Estado del Estudiante:** En evaluación de Defensa Técnica (Respuestas Completadas)
**Instrucciones:** 
1. Lee detenidamente el código base simulado y los retos presentados a continuación.
2. Modifica el código de cada sección para resolver el reto correspondiente.
3. Escribe tus respuestas justificadas debajo de cada bloque de código en la sección señalada como `> [RESPUESTA ORAL Y JUSTIFICACIÓN TÉCNICA]`.
4. Sé extremadamente riguroso y técnico. Debes justificar tus elecciones de diseño, patrones, manejo de excepciones y transacciones utilizando terminología de arquitectura de software (Spring Boot, Clean Architecture, REST, Mockito, JPA/Hibernate).

---

## CÓDIGO BASE DEL MICROSERVICIO

El microservicio `galaxy-registry-service` se encarga de la gestión y registro de naves de la Federación Galáctica y su comunicación con centros de control externos.

### Entidades y DTOs
```java
// Nave.java
package com.federacion.registry.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "naves")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nombre;
    private String matricula; // Formato: REG-XXXX (X = número)
    private String clase; // EXPLORER, WARSHIELD, CARGO
    private Double capacidadCargaMax;
    private Double cargaActual;
    private Integer potenciaMotor;
    private Double capacidadCombustible;
    private Double rangoVuelo; // Autonomía en años luz
}

// CoordenadasDTO.java
package com.federacion.registry.dto;

import lombok.Data;

@Data
public class CoordenadasDTO {
    private String planetaDestino;
    private Double latitudEspacial;
    private Double longitudEspacial;
    private String cuadrante;
    private boolean sectorSeguro;
}
```

### Capa de Acceso a Datos y Comunicación Remota
```java
// NaveRepository.java
package com.federacion.registry.repository;

import com.federacion.registry.model.Nave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NaveRepository extends JpaRepository<Nave, Long> {
    Optional<Nave> findByMatricula(String matricula);
}

// CentroControlClient.java (Feign Client)
package com.federacion.registry.client;

import com.federacion.registry.dto.CoordenadasDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "centro-control-service", url = "http://api.control-naboo.federacion:8080/api/v1")
public interface CentroControlClient {

    @GetMapping("/coordenadas/{planeta}")
    CoordenadasDTO obtenerCoordenadas(@PathVariable("planeta") String planeta);
}
```

### Capa de Negocio (Servicio)
```java
// NaveService.java
package com.federacion.registry.service;

import com.federacion.registry.client.CentroControlClient;
import com.federacion.registry.dto.CoordenadasDTO;
import com.federacion.registry.model.Nave;
import com.federacion.registry.repository.NaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NaveService {

    private final NaveRepository naveRepository;
    private final CentroControlClient centroControlClient;

    @Transactional(readOnly = true)
    public Nave obtenerPorMatricula(String matricula) {
        return naveRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RuntimeException("Nave no encontrada"));
    }

    @Transactional
    public Nave registrarNave(Nave nave) {
        if (nave.getCargaActual() > nave.getCapacidadCargaMax()) {
            throw new IllegalArgumentException("La carga actual supera el límite permitido.");
        }
        return naveRepository.save(nave);
    }
}
```

### Capa de Presentación (Controlador)
```java
// NaveController.java
package com.federacion.registry.controller;

import com.federacion.registry.model.Nave;
import com.federacion.registry.service.NaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/naves")
@RequiredArgsConstructor
public class NaveController {

    private final NaveService naveService;

    @PostMapping
    public ResponseEntity<Nave> registrar(@RequestBody Nave nave) {
        return new ResponseEntity<>(naveService.registrarNave(nave), HttpStatus.CREATED);
    }

    @GetMapping("/{matricula}")
    public ResponseEntity<Nave> obtenerPorMatricula(@PathVariable String matricula) {
        return ResponseEntity.ok(naveService.obtenerPorMatricula(matricula));
    }
}
```

---

## RETO 1 - PRUEBAS UNITARIAS (IE 3.1.2)

### Contexto
Se te presenta la siguiente prueba unitaria implementada en el microservicio. Utiliza JUnit 5 y Mockito bajo la estructura estructurada **Given-When-Then** (patrón AAA: Arrange-Act-Assert).

```java
// NaveServiceTest.java
package com.federacion.registry.service;

import com.federacion.registry.model.Nave;
import com.federacion.registry.repository.NaveRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NaveServiceTest {

    @Mock
    private NaveRepository naveRepository;

    @InjectMocks
    private NaveService naveService;

    @Test
    @DisplayName("Debería lanzar excepción cuando la carga supera el máximo")
    void registrarNave_CargaExcedeMaximo_LanzaIllegalArgumentException() {
        // Given
        Nave naveInvalida = Nave.builder()
                .nombre("Milano")
                .capacidadCargaMax(100.0)
                .cargaActual(120.0)
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            naveService.registrarNave(naveInvalida);
        });

        assertEquals("La carga actual supera el límite permitido.", exception.getMessage());
        verifyNoInteractions(naveRepository); // Asegura que no se interactúa con el DB en fallos lógicos
    }
}
```

### Tu Tarea
1. **Explicación**: Explica conceptualmente por qué se usa `verifyNoInteractions(naveRepository)` en esta prueba y qué beneficio aporta a nivel de diseño de arquitectura y costos de ejecución de CI/CD.
2. **Implementación**: Escribe una nueva prueba unitaria/funcional en el bloque de abajo que verifique el registro **exitoso** de una nave, aplicando también el patrón Given-When-Then y mockeando adecuadamente el comportamiento del repositorio.

```java
// Escribe aquí tu nueva prueba unitaria para el registro exitoso:
@Test
@DisplayName("Debería registrar la nave exitosamente cuando los datos son válidos")
void registrarNave_DatosValidos_RetornaNaveRegistrada() {
    // Given
    Nave naveIngresada = Nave.builder()
            .nombre("Discovery")
            .matricula("REG-1234")
            .capacidadCargaMax(500.0)
            .cargaActual(200.0)
            .clase("EXPLORER")
            .potenciaMotor(400)
            .capacidadCombustible(100.0)
            .build();
            
    Nave navePersistida = Nave.builder()
            .id(1L)
            .nombre("Discovery")
            .matricula("REG-1234")
            .capacidadCargaMax(500.0)
            .cargaActual(200.0)
            .clase("EXPLORER")
            .potenciaMotor(400)
            .capacidadCombustible(100.0)
            .rangoVuelo(960.0) // Cálculo esperado: ((400 * 1.5) + (100 * 2.0)) * 1.2 = (600 + 200) * 1.2 = 960
            .build();

    when(naveRepository.save(any(Nave.class))).thenReturn(navePersistida);

    // When
    Nave naveResultado = naveService.registrarNave(naveIngresada);

    // Then
    assertNotNull(naveResultado);
    assertEquals(1L, naveResultado.getId());
    assertEquals(960.0, naveResultado.getRangoVuelo());
    verify(naveRepository, times(1)).save(any(Nave.class));
}
```

> **[RESPUESTA ORAL Y JUSTIFICACIÓN TÉCNICA - RETO 1]**
> El uso del método `verifyNoInteractions(naveRepository)` en pruebas unitarias es una excelente práctica de diseño defensivo y arquitectónico:
> 
> 1. **Principio de Fail-Fast (Fallo Rápido)**: Valida que la lógica de negocio se ejecute y corte el flujo en la memoria antes de llegar a la capa de infraestructura. Si hubiese interacciones con el repositorio en una validación fallida, implicaría que hay un error de flujo lógico que intenta realizar una persistencia innecesaria.
> 2. **Aislamiento y Pureza Unitaria**: Nos asegura que la prueba se mantiene 100% aislada de operaciones de I/O o base de datos.
> 3. **Optimización de Pipelines de CI/CD**: Evita el desperdicio de ciclos de CPU en mocks complejos y reduce la probabilidad de falsos positivos en las pruebas unitarias debido a dependencias colaterales. A nivel de infraestructura distribuidas y bases de datos reales, asegura que no consumiremos conexiones del connection pool para peticiones que de origen son inválidas.

---

## RETO 2 - MODIFICACIÓN SORPRESA CRUD (IE 2.1.4)

### Contexto
La Federación Galáctica ha emitido una directiva de seguridad urgente (Regla de Negocio 701-B): 
*El campo `rangoVuelo` (autonomía en años luz) ya no puede enviarse directamente en el payload de creación de la nave. Debe calcularse automáticamente en el servidor durante la operación de guardado.*
*La fórmula obligatoria es:* `rangoVuelo = (potenciaMotor * 1.5) + (capacidadCombustible * 2.0)`.
*Además, si el tipo de nave es `"EXPLORER"`, el `rangoVuelo` resultante debe multiplicarse por un factor adicional de `1.2` debido a sus motores de exploración optimizados.*

### Tu Tarea
Modifica el método `registrarNave(Nave nave)` en la clase `NaveService` para que implemente este cálculo dinámico antes de persistir la entidad, asegurando que si un cliente intenta enviar un valor precalculado de `rangoVuelo` en el JSON, este sea ignorado y sobreescrito por la regla del servidor.

```java
// Copia aquí el método modificado de NaveService:
@Transactional
public Nave registrarNave(Nave nave) {
    if (nave.getCargaActual() > nave.getCapacidadCargaMax()) {
        throw new IllegalArgumentException("La carga actual supera el límite permitido.");
    }
    
    // Regla de Negocio 701-B: Sobreescribir rangoVuelo calculándolo en el servidor
    int potencia = nave.getPotenciaMotor() != null ? nave.getPotenciaMotor() : 0;
    double combustible = nave.getCapacidadCombustible() != null ? nave.getCapacidadCombustible() : 0.0;
    
    double rangoCalculado = (potencia * 1.5) + (combustible * 2.0);
    
    if ("EXPLORER".equalsIgnoreCase(nave.getClase())) {
        rangoCalculado *= 1.2;
    }
    
    nave.setRangoVuelo(rangoCalculado); // Ignora el valor del payload del cliente
    
    return naveRepository.save(nave);
}
```

> **[RESPUESTA ORAL Y JUSTIFICACIÓN TÉCNICA - RETO 2]**
> Decidí implementar el cálculo en la capa de Servicio (`NaveService`) por varias justificaciones técnicas de arquitectura:
> 
> 1. **Desacoplamiento y Portabilidad de Base de Datos**: Evitamos el uso de base de datos triggers o funciones almacenadas (Stored Procedures). Los triggers generan un fuerte acoplamiento con el proveedor de base de datos (vendor lock-in) y dificultan la realización de pruebas unitarias locales en memoria (por ejemplo, con H2 o bases de datos livianas de test).
> 2. **Integridad del Contexto de Persistencia (First-Level Cache de Hibernate)**: Si hiciéramos el cálculo vía base de datos (trigger), el objeto en memoria (la entidad gestionada por JPA/Hibernate) no tendría el valor correcto a menos que llamemos explícitamente a `entityManager.refresh(nave)`. Esto generaría un viaje de red extra (Round-trip) hacia la base de datos, incrementando la latencia del microservicio.
> 3. **Responsabilidad Única del Controlador**: El controlador REST debe centrarse en recibir la petición HTTP, verificar la estructura sintáctica del payload y retornar el código HTTP semántico correspondiente (ej. 201 Created). La lógica de negocio operacional y el cálculo de propiedades es responsabilidad exclusiva del dominio y sus servicios.

---

## RETO 3 - JUSTIFICACIÓN DE LÓGICA (IE 2.2.2)

### Contexto
Para evitar registros de contrabando interplanetario, la Federación exige una nueva regla de validación estricta de datos de entrada:
*La matrícula de una nave debe seguir el formato exacto `REG-` seguido de 4 números dígitos (ejemplo: `REG-4209`). Si el formato no coincide, no se debe procesar el guardado.*

### Tu Tarea
1. Implementa la lógica de validación por expresión regular en la capa correspondiente del microservicio.
2. Justifica detalladamente en qué capa decidiste colocar esta validación (¿Entidad JPA con `@Pattern`, anotaciones del DTO con `@Valid`, o lógica procedural de servicio con `matches()`) y cuál es el impacto de este cambio en el flujo interno de transacciones del microservicio.

```java
// Copia aquí el fragmento de código donde implementas la validación:

// Opción recomendada: Validación sintáctica en la capa de transporte utilizando Bean Validation (DTO/Entidad)
// y validación semántica de refuerzo en el Servicio.

// 1. Modificación en la Entidad (o en su DTO respectivo)
package com.federacion.registry.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

// Dentro de la clase Nave:
@NotNull(message = "La matrícula no puede ser nula")
@Pattern(regexp = "^REG-\\d{4}$", message = "La matrícula debe cumplir con el formato REG-XXXX (donde X son dígitos)")
private String matricula;

// 2. Modificación de seguridad procedural en NaveService.java
@Transactional
public Nave registrarNave(Nave nave) {
    if (nave.getMatricula() == null || !nave.getMatricula().matches("^REG-\\d{4}$")) {
        throw new IllegalArgumentException("El formato de matrícula proporcionado no es válido.");
    }
    // ... resto del método
}
```

> **[RESPUESTA ORAL Y JUSTIFICACIÓN TÉCNICA - RETO 3]**
> Para esta validación opté por un enfoque híbrido: validación declarativa en la capa del DTO/Entidad con `@Pattern` y validación programática de refuerzo en la capa de negocio (`NaveService.java`).
> 
> * **Fail-Fast Temprano (Controlador)**: Al anotar el campo con `@Pattern` y validar en el controlador con `@Valid`, Spring intercepta la solicitud incorrecta en el filtro de entrada antes de asignarle recursos adicionales de procesamiento (hilos de ejecución del servicio). Esto reduce la latencia general del sistema y la sobrecarga innecesaria.
> * **Impacto Transaccional**: Si la validación se hiciera puramente a nivel de la base de datos (con restricciones tipo `CHECK`), la transacción de Spring Boot (`@Transactional`) ya se habría abierto, consumiendo una conexión del connection pool (HikariCP). Al fallar la inserción SQL, la transacción se marca obligatoriamente para Rollback y la base de datos ejecuta operaciones de descarte. Realizando la validación en la JVM (antes de persistir), evitamos consumir y bloquear recursos transaccionales de la base de datos en peticiones inválidas.

---

## RETO 4 - COMUNICACIÓN REMOTA (IE 2.4.2)

### Contexto
El microservicio `galaxy-registry-service` utiliza un cliente Feign (`CentroControlClient`) para comunicarse con la API de Naboo y verificar si el planeta destino está en un sector seguro antes de permitir que la nave despegue.

### Tu Tarea
1. Explica técnicamente cómo gestiona Spring Boot y OpenFeign el ciclo de vida de la petición HTTP externa y qué hilos de ejecución intervienen por defecto.
2. Justifica el uso del DTO `CoordenadasDTO` en la interfaz Feign en lugar de mapear directamente la entidad remota.
3. Modifica la llamada de OpenFeign para que ahora reciba la coordenada destino parametrizada por `longitud` y `latitud` en lugar de por el nombre del planeta (ej. `/coordenadas/buscar?lat=X&lon=Y`). Proporciona el código de la nueva interfaz Feign y su llamada en el servicio.

```java
// Escribe aquí la interfaz Feign modificada y su correspondiente integración en el servicio:

// CentroControlClient.java (Modificado)
package com.federacion.registry.client;

import com.federacion.registry.dto.CoordenadasDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "centro-control-service", url = "http://api.control-naboo.federacion:8080/api/v1")
public interface CentroControlClient {

    @GetMapping("/coordenadas/buscar")
    CoordenadasDTO obtenerCoordenadasPorPosicion(
            @RequestParam("lat") Double latitud,
            @RequestParam("lon") Double longitud
    );
}

// NaveService.java (Integración y flujo de consumo)
@Transactional(readOnly = true)
public boolean verificarSeguridadRuta(Double latitud, Double longitud) {
    CoordenadasDTO coordenadas = centroControlClient.obtenerCoordenadasPorPosicion(latitud, longitud);
    return coordenadas != null && coordenadas.isSectorSeguro();
}
```

> **[RESPUESTA ORAL Y JUSTIFICACIÓN TÉCNICA - RETO 4]**
> **1. Ciclo de Vida e Hilos de Feign**:
> En Spring Boot (usando Tomcat embebido), cada petición entrante es procesada en un hilo del pool de conexiones HTTP (`nio-exec-*`). Cuando este hilo invoca un método de OpenFeign, la llamada se realiza de forma **síncrona y bloqueante** por defecto. El hilo de Tomcat se suspende a la espera de la respuesta I/O del servidor externo. Si el servicio de Naboo experimenta latencia o caídas, y no configuramos adecuadamente los timeouts (`connect-timeout` y `read-timeout`), todos los hilos de Tomcat pueden quedar bloqueados indefinidamente, provocando la denegación de servicio (DoS) del microservicio local.
> 
> **2. Justificación del DTO**:
> La utilización de `CoordenadasDTO` cumple el principio de **Desacoplamiento Técnico (Low Coupling)**. La entidad interna del servicio remoto de Naboo puede incluir anotaciones JPA, auditoría, IDs y lógica propia de su dominio. Mapear directamente esa entidad acoplaría nuestro microservicio cliente al esquema del base de datos de Naboo. Si ellos cambian su base de datos o su motor de persistencia, nuestro microservicio fallaría. El DTO actúa como un contrato de interfaz de red inmutable y limpio.
> 
> **3. Cambio de Firma (Query Params vs Path Variables)**:
> Cambiar a parámetros de consulta (`@RequestParam`) es más adecuado para operaciones de filtrado y búsqueda paramétrica multidimensional (como latitud y longitud). Esto permite una API RESTful más limpia (`/buscar?lat=X&lon=Y`) en lugar de rutas dinámicas complejas con múltiples Path Variables, mejorando la extensibilidad futura del servicio remoto (ej. añadir un tercer parámetro de altitud espacial `alt` sin alterar el patrón del Path).

---

## RETO 5 - CORRECCIÓN DE ERRORES (IE 2.3.2)

### Contexto
Durante las pruebas de integración en el entorno de Staging, el servidor central de la Federación Galáctica empezó a retornar el siguiente log de error crítico en la consola de Spring Boot:

```log
2026-07-14 18:02:15.342 ERROR 12940 --- [io-8080-exec-4] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is feign.FeignException$NotFound: [404 Not Found] during [GET] to [http://api.control-naboo.federacion:8080/api/v1/coordenadas/Tatooine] [CentroControlClient#obtenerCoordenadas(String)]] with root cause

feign.FeignException$NotFound: [404 Not Found] during [GET] to [http://api.control-naboo.federacion:8080/api/v1/coordenadas/Tatooine] [CentroControlClient#obtenerCoordenadas(String)]
    at feign.FeignException.clientErrorStatus(FeignException.java:219) ~[feign-core-12.1.jar:na]
    at feign.FeignException.errorStatus(FeignException.java:194) ~[feign-core-12.1.jar:na]
    at feign.codec.ErrorDecoder$Default.decode(ErrorDecoder.java:92) ~[feign-core-12.1.jar:na]
    ...
    at com.federacion.registry.service.NaveService.validarDestinoSeguro(NaveService.java:42) ~[classes/:na]
```

El flujo de despegue de la nave se interrumpe abruptamente con un código de respuesta HTTP `500 Internal Server Error` devuelto al cliente que hace la solicitud, lo cual es inaceptable para la experiencia del usuario y las directivas de tolerancia a fallos.

### Tu Tarea
1. Identifica el error raíz y explica por qué se está propagando como una excepción `500` al cliente final.
2. Implementa una solución en el código para interceptar este error específico de Feign de manera limpia (por ejemplo, devolviendo un código HTTP adecuado como `404` con un mensaje estructurado, o aplicando un fallback/resiliencia). Puedes implementar un `ErrorDecoder` personalizado, un `@ControllerAdvice`, o control de excepciones en el Service.
3. Explica el impacto del ajuste y cómo mejora la resiliencia y el comportamiento del microservicio ante caídas del sistema externo.

```java
// Copia aquí el código con tu solución de manejo de excepciones (Controlador, Advice o Service):

// 1. Excepción de negocio personalizada
package com.federacion.registry.exception;

public class RecursoRemotoNoEncontradoException extends RuntimeException {
    public RecursoRemotoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

// 2. Decoder personalizado de Feign para traducir el 404 a una excepción de negocio
package com.federacion.registry.config;

import com.federacion.registry.exception.RecursoRemotoNoEncontradoException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new RecursoRemotoNoEncontradoException(
                "El planeta o sector de control remoto consultado no fue encontrado en los registros externos."
            );
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}

// 3. Interceptor Global en el Controlador para traducir a código HTTP 404 estructurado
package com.federacion.registry.controller;

import com.federacion.registry.exception.RecursoRemotoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoRemotoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoRemotoNoEncontrado(RecursoRemotoNoEncontradoException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        response.put("path", "/api/v1/naves");
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
```

> **[RESPUESTA ORAL Y JUSTIFICACIÓN TÉCNICA - RETO 5]**
> **Identificación del Problema**:
> El microservicio remoto de Naboo retornó un código `404 Not Found` porque el planeta "Tatooine" no existe. Feign, al recibir este error HTTP en su pipeline de decodificación predeterminado, arroja una excepción `FeignException$NotFound`. Dado que esta excepción no está capturada por ninguna cláusula try-catch en el Service ni por un manejador de excepciones del Controller, sube hasta la infraestructura de Spring MVC, la cual asume que es una falla interna inesperada y la envuelve en una respuesta `500 Internal Server Error`.
> 
> **Defensa de la Solución**:
> 1. **Uso de Custom ErrorDecoder**: Permite desacoplar el microservicio de las excepciones técnicas de bajo nivel de la biblioteca de comunicación (OpenFeign). Traducimos el error HTTP HTTP 404 a una excepción semántica de negocio (`RecursoRemotoNoEncontradoException`) directamente al salir del cliente Feign.
> 2. **RestControllerAdvice**: Centraliza el control de excepciones de forma transversal (AOP). Así, si surgen otros clientes Feign en el futuro, no duplicamos bloques try-catch repetitivos en toda la capa de servicio. Devolvemos un payload JSON estructurado con el código REST semántico correcto (`404 Not Found`) en lugar de un `500`, previniendo la fuga de información sensible (como trazas del sistema e IPs de servicios internos) y cumpliendo las directivas de seguridad corporativas de la Federación Galáctica.

---

## EVALUACIÓN Y DEFENSA DE DECISIONES TÉCNICAS

Una vez que completes las implementaciones y completes cada bloque de **[RESPUESTA ORAL Y JUSTIFICACIÓN TÉCNICA]**, envía tus respuestas. 
Como evaluador estricto pero justo, analizaré rigurosamente tu código y tus argumentos técnicos para dictaminar si estás calificado para liderar el desarrollo de la arquitectura de la Federación Galáctica, y procederé a realizarte **preguntas de presión basadas en tus respuestas**. 

¡El tiempo corre, ingeniero! Demuestra de qué estás hecho.
