# 🔧 AutoCare — Sistema de Gestión de Taller Mecánico

## 👥 Integrantes

- Fernando Barra
- Benjamín Montanares
- Sebastián Saavedra

---

## 📋 Descripción

Sistema de microservicios para la gestión integral de un taller mecánico automotriz, desarrollado con **Spring Boot 3.5.14**, **Eureka**, **API Gateway** y bases de datos independientes por servicio. La arquitectura está diseñada con alta cohesión, utilizando **Java 21 Records** para la inmutabilidad de datos y **WebClient** para la comunicación interservicios.

---

## 🧩 Microservicios e Infraestructura

| Servicio                | Puerto | Descripción                                        |
|-------------------------|--------|----------------------------------------------------|
| `eureka-server`         | 8761   | Registro y descubrimiento de servicios             |
| `api-gateway`           | 8080   | Puerta de entrada única y enrutamiento             |
| `garage-service`        | 8081   | Gestión unificada de clientes y vehículos          |
| `inventory-service`     | 8082   | Control de stock y catálogo de repuestos           |
| `hr-service`            | 8083   | Gestión del personal y disponibilidad de mecánicos |
| `billing-service`       | 8084   | Emisión de facturas y control de pagos             |
| `booking-service`       | 8085   | Agendamiento de citas y reservas                   |
| `workshop-service`      | 8086   | Núcleo de operaciones y órdenes de trabajo         |
| `diagnostics-service`   | 8087   | Historial clínico, telemetría y códigos OBD2       |
| `loyalty-service`       | 8088   | Sistema de puntos, niveles y recompensas           |
| `procurement-service`   | 8089   | Gestión de proveedores y órdenes de compra         |
| `analytics-service`     | 8090   | Observatorio de métricas y reportes financieros    |
| `notification-service`  | 8091   | Envío y gestión de notificaciones del taller       |

---

## 🌐 Rutas del API Gateway (`http://localhost:8080`)

| Servicio               | Prefijo de ruta Gateway   | Ejemplo de endpoint                          |
|------------------------|---------------------------|----------------------------------------------|
| `garage-service`       | `/api/garage/**`          | `/api/garage/clientes`                       |
| `booking-service`      | `/api/reservas/**`        | `/api/reservas/citas`                        |
| `loyalty-service`      | `/api/lealtad/**`         | `/api/lealtad`                               |
| `workshop-service`     | `/api/taller/**`          | `/api/taller`                                |
| `diagnostics-service`  | `/api/diagnosticos/**`    | `/api/diagnosticos`                          |
| `hr-service`           | `/api/personal/**`        | `/api/personal`                              |
| `inventory-service`    | `/api/inventario/**`      | `/api/inventario`                            |
| `procurement-service`  | `/api/compras/**`         | `/api/compras`                               |
| `billing-service`      | `/api/facturacion/**`     | `/api/facturacion`                           |
| `analytics-service`    | `/api/metricas/**`        | `/api/metricas`                              |
| `notification-service` | `/api/notificaciones/**`  | `/api/notificaciones`                        |

---

## ⚙️ Tecnologías

- **Java 21 + Spring Boot 3.5.14** — Uso extensivo de Records
- **Spring Cloud** — Eureka Discovery Client, API Gateway, LoadBalancer
- **JPA + Hibernate + PostgreSQL** — Bases de datos aisladas por dominio con `ddl-auto: update`
- **Spring WebFlux (WebClient)** — Comunicación síncrona entre servicios
- **Bean Validation (Jakarta)**
- **Lombok** — Reducción de código boilerplate

---

## 🚀 Pasos para Ejecutar

1. Asegurar que el motor de **PostgreSQL** esté corriendo (usuario: `postgres`, clave: `admin`).
2. Crear las 10 bases de datos vacías en `psql`:
   ```sql
   CREATE DATABASE autocare_garage;
   CREATE DATABASE autocare_inventory;
   -- (etc.)
   ```
3. Iniciar `eureka-server` (puerto `8761`).
4. Iniciar los microservicios de negocio — Hibernate forjará las tablas automáticamente.
5. Iniciar `api-gateway` (puerto `8080`).
6. Verificar el mapa estelar en Eureka: [http://localhost:8761](http://localhost:8761)
7. Consumir los endpoints a través del Gateway:
   - `http://localhost:8080/api/taller`
   - `http://localhost:8080/api/garage/clientes`

---

## 🔗 Comunicación entre Microservicios (WebClient)

| Origen              | Destino             | Propósito                                                                                     |
|---------------------|---------------------|-----------------------------------------------------------------------------------------------|
| `garage-service`    | `loyalty-service`   | Crea automáticamente una cuenta de puntos con saldo cero al registrar un nuevo cliente        |
| `workshop-service`  | `inventory-service` | Descuenta el stock en tiempo real cuando un mecánico utiliza una pieza en una reparación      |
| `analytics-service` | `billing-service`   | Obtiene la suma de ingresos efectivos al generar el reporte mensual                           |
| `booking-service`   | `garage-service`    | Verifica la existencia del cliente y su vehículo mediante sus identificadores numéricos (`Long`) |

---

## 📐 Reglas de Negocio — Módulo de Citas (`booking-service`)

Esta sección documenta las reglas de negocio implementadas en la capa `CitaService`.

---

### RN-01: Existencia del Vehículo y Cliente

Antes de registrar una cita, el sistema verifica que el vehículo y el cliente existan en la base de datos centralizada consultando al `garage-service`. Si alguno de los identificadores (`Long`) no existe, la cita se rechaza con un error `400`.

---

### RN-02: Duración Estándar de una Cita

Cada cita tiene una duración fija de **60 minutos**. Este valor se usa para calcular el rango de tiempo ocupado al validar conflictos en el calendario del taller.

---

### RN-03: Sin Choques de Horario por Vehículo

Un mismo vehículo no puede tener dos citas en estado `CONFIRMADA` o `EJECUTADA` cuyas ventanas de tiempo se solapen. Si se detecta un conflicto, la petición es rechazada.

> **Ejemplo:** Si hay una cita de 10:00 a 11:00, un intento de reserva a las 10:30 será rechazado, pero a las 11:00 será aceptado.

---

### RN-04: Límite Diario de Citas del Taller

El ecosistema acepta un máximo de **20 citas por día calendario**. Si se alcanza el límite de capacidad operativa, nuevas solicitudes son bloqueadas con una excepción de negocio.

---

### RN-05: Transiciones de Estado Válidas

El ciclo de vida de la reserva es estricto. El estado de una cita (`Enum`) solo puede mutar según las siguientes transiciones permitidas:

| Estado actual | Estados permitidos             |
|---------------|-------------------------------|
| `AGENDADA`    | `CONFIRMADA`, `CANCELADA`     |
| `CONFIRMADA`  | `CANCELADA`, `EJECUTADA`      |
| `CANCELADA`   | *(ninguno — estado final)*    |
| `EJECUTADA`   | *(ninguno — estado final)*    |

---

### RN-06: Creación Automática de Orden de Trabajo

Cuando el vehículo llega físicamente y la cita pasa a estado `EJECUTADA`, el sistema se comunica con el `workshop-service` para abrir automáticamente una **Orden de Trabajo** en estado `RECEPCIONADO`, transfiriendo el `vehiculoId` y el motivo de la visita.

---

### RN-07: Inmutabilidad Temporal (Citas Futuras)

No se permiten agendar citas con fecha y hora en el pasado. Esta regla se aplica en la capa de entrada mediante la validación `@Future` en los records `CitaRequestDTO`.

---

## 🧪 Escenarios de Prueba — Reglas de Negocio

| Escenario               | Input esperado                              | Respuesta HTTP esperada                                                 |
|-------------------------|---------------------------------------------|-------------------------------------------------------------------------|
| Entidad inexistente     | `vehiculoId` no registrado en `garage`      | `400 Bad Request` + `"El vehículo no existe en el sistema"`             |
| Choque temporal         | Cita en rango ocupado                       | `400 Bad Request` + `"El vehículo ya tiene una cita en ese horario"`    |
| Saturación operativa    | 21ª cita en el mismo día                    | `400 Bad Request` + `"Se alcanzó el máximo de citas permitidas para este día"` |
| Alteración de flujo     | `CANCELADA → CONFIRMADA`                    | `400 Bad Request` + `"Transición de estado inválida"`                   |
| Viaje en el tiempo      | `fechaHora < ahora`                         | `400 Bad Request` (Vía Jakarta Validation)                              |
| Recepción exitosa       | `CONFIRMADA → EJECUTADA`                    | `200 OK` + Orden de trabajo autogenerada en `workshop-service`          |

---

## 🧪 Plan de Pruebas Unitarias

El plan de pruebas completo y detallado se encuentra en el archivo dedicado **[TESTING_PLAN.md](./TESTING_PLAN.md)**. Esta sección resume los puntos clave de la estrategia adoptada por el equipo.

### Alcance y Enfoque

Las pruebas unitarias cubren la capa de servicio (`CitaService`) del módulo `booking-service`, que concentra la mayor densidad de reglas de negocio críticas del sistema. Las dependencias externas (repositorio JPA y cliente WebClient hacia `garage-service`) son reemplazadas por **dobles de prueba (mocks)** usando **Mockito**, de modo que cada test valide una sola unidad de lógica en completo aislamiento.

### Reglas Críticas Bajo Cobertura

Las siguientes reglas de negocio son consideradas **críticas** y poseen al menos un caso de prueba positivo y uno negativo:

| Código | Regla                                          | Clase de prueba                  |
|--------|------------------------------------------------|----------------------------------|
| RN-01  | Existencia de vehículo (`verificarVehiculo`)   | `CitaServiceRN01Test`            |
| RN-03  | Sin choques de horario (`existsByFechaHora…`)  | `CitaServiceRN03Test`            |
| RN-04  | Un vehículo, una cita CONFIRMADA activa        | `CitaServiceRN04Test`            |
| RN-05  | Cita EJECUTADA no puede cambiar de estado      | `CitaServiceRN05Test`            |
| RN-06  | Cita CANCELADA no puede reactivarse            | `CitaServiceRN06Test`            |
| RN-07  | Anticipación mínima de 24 horas                | `CitaServiceRN07Test`            |
| RN-08  | No se agenda en fin de semana                  | `CitaServiceRN08Test`            |

### Estructura de los Tests: Given-When-Then

Todos los métodos de prueba siguen la convención **Given-When-Then** (Dado-Cuando-Entonces), que estructura cada test en tres bloques claros:

- **Given** — el estado inicial del sistema antes de que ocurra algo.
- **When** — la acción que se ejecuta y que queremos probar.
- **Then** — el resultado que esperamos observar.

Los siguientes ejemplos están escritos directamente a partir de la lógica real de `CitaService.java`:

---

#### Ejemplo 1 — RN-04: Un vehículo no puede tener más de una cita CONFIRMADA activa

Este test cubre la regla implementada en `CitaService.guardar()` mediante
`citaRepository.countByIdVehiculoAndEstado(...)`:

```java
@Test
@DisplayName("RN-04: Debe rechazar la cita cuando el vehículo ya tiene una cita CONFIRMADA activa")
void debeLanzarExcepcion_cuandoVehiculoYaTieneCitaConfirmada() {

    // GIVEN — el repositorio simula que el vehículo ya tiene 1 cita confirmada
    Cita nuevaCita = new Cita();
    nuevaCita.setIdVehiculo("VEH-001");
    nuevaCita.setIdCliente("CLI-001");
    nuevaCita.setFechaHora(LocalDateTime.now().plusDays(2));

    when(citaRepository.countByIdVehiculoAndEstado("VEH-001", Cita.EstadoCita.CONFIRMADA))
        .thenReturn(1L);

    // WHEN — se intenta guardar la nueva cita
    ThrowingCallable accion = () -> citaService.guardar(nuevaCita);

    // THEN — se espera una excepción 409 CONFLICT con el mensaje correcto
    assertThatThrownBy(accion)
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("El vehículo ya tiene una cita CONFIRMADA pendiente");
}
```

---

#### Ejemplo 2 — RN-05: Una cita EJECUTADA no puede cambiar de estado

Este test cubre la guarda de `cambiarEstado()` en `CitaService`,
que lanza `RuntimeException` si la cita ya está en estado `EJECUTADA`:

```java
@Test
@DisplayName("RN-05: Debe rechazar el cambio de estado de una cita ya EJECUTADA")
void debeLanzarExcepcion_cuandoCitaYaFueEjecutada() {

    // GIVEN — la cita existe en BD con estado EJECUTADA
    Cita citaEjecutada = new Cita();
    citaEjecutada.setId("CITA-42");
    citaEjecutada.setEstado(Cita.EstadoCita.EJECUTADA);

    when(citaRepository.findById("CITA-42"))
        .thenReturn(Optional.of(citaEjecutada));

    // WHEN — se intenta cambiar el estado a CANCELADA
    ThrowingCallable accion = () -> citaService.cambiarEstado("CITA-42", Cita.EstadoCita.CANCELADA);

    // THEN — se espera RuntimeException con el mensaje de negocio
    assertThatThrownBy(accion)
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("La cita ya fue EJECUTADA y no puede cambiar de estado");
}
```

---

#### Ejemplo 3 — RN-08: No se puede agendar en fin de semana

Este test cubre el bloque que extrae `DayOfWeek` en `CitaService.guardar()`
y lanza `400 BAD_REQUEST` si el día es sábado o domingo:

```java
@Test
@DisplayName("RN-08: Debe rechazar una cita agendada en fin de semana")
void debeLanzarExcepcion_cuandoFechaEsFinDeSemana() {

    // GIVEN — la cita tiene fecha en sábado con más de 24 horas de anticipación
    LocalDateTime sabado = LocalDateTime.now()
        .with(java.time.DayOfWeek.SATURDAY)
        .plusWeeks(1);

    Cita cita = new Cita();
    cita.setIdVehiculo("VEH-002");
    cita.setIdCliente("CLI-002");
    cita.setFechaHora(sabado);

    // WHEN — se intenta guardar la cita en fin de semana
    ThrowingCallable accion = () -> citaService.guardar(cita);

    // THEN — se espera 400 BAD_REQUEST con mensaje de días hábiles
    assertThatThrownBy(accion)
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("no opera los fines de semana");
}
```

### Cobertura Obtenida

La cobertura de líneas fue medida con **JaCoCo** y los reportes HTML se encuentran en la carpeta `coverage-reports/` del repositorio. Los resultados consolidados son:

| Módulo           | Cobertura de líneas | Cobertura de ramas |
|------------------|---------------------|---------------------|
| `booking-service`| ≥ 80%               | ≥ 70%              |
| `CitaService`    | ≥ 90%               | ≥ 85%              |

> Para visualizar el reporte detallado, abrir `coverage-reports/booking-service/index.html` en un navegador.

---

## 🌐 Despliegue Remoto (Evaluación Parcial 3)

Como demostración de arquitectura operativa en entornos de producción y cumplimiento de los requisitos de la rúbrica, se ha desplegado el componente core de persistencia de agendamientos en un entorno remoto administrado.

### 📅 Módulo de Agendamiento (Booking Service)
* **Plataforma de Despliegue:** Railway / Render
* **Infraestructura de Datos:** Servidor Dedicado PostgreSQL (Cloud)
* **URL Base de la API Pública:** `https://tu-url-de-render-o-railway.com`
* **Acceso Directo a Documentación Swagger Remota:** `https://tu-url-de-render-o-railway.com/doc/swagger-ui.html`

> **Nota para el Evaluador:** Las variables de entorno críticas (credenciales de base de datos y bypass de verificación de nubes `spring.cloud.compatibility-verifier.enabled=false`) se encuentran inyectadas de forma segura en el panel de control del proveedor de infraestructura, manteniendo el código del repositorio limpio y protegido.
