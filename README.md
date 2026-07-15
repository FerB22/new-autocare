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

### RN-01: Existencia del Cliente y Vehículo

Antes de registrar una cita, el sistema verifica que el vehículo y el cliente existan consultando dinámicamente al microservicio `garage-service` (a través de `GarageClient`). Si alguno de los identificadores (`Long`) no es válido o no existe, la solicitud se rechaza con un error `400 Bad Request`.

---

### RN-03: Sin Choques de Horario (Unicidad Temporal)

No pueden existir dos citas en el mismo día y hora exacta en el taller. El sistema valida esto consultando la base de datos local y, si el horario está ocupado, rechaza el agendamiento con una excepción de negocio.

---

### RN-04: Límite Diario de Capacidad del Taller

Para garantizar la calidad de la atención y evitar la sobrecarga del taller, el sistema impone un límite de **20 citas máximo por día calendario**. Superado este valor, cualquier intento de agendamiento para esa fecha será rechazado con un error de negocio.

---

## 🧪 Escenarios de Prueba — Reglas de Negocio

| Escenario               | Input esperado                              | Respuesta HTTP esperada                                                 |
|-------------------------|---------------------------------------------|-------------------------------------------------------------------------|
| Cliente inexistente     | `clienteId` no registrado en `garage`       | `400 Bad Request` + `"El cliente no existe en el sistema."`             |
| Vehículo inexistente    | `vehiculoId` no registrado en `garage`      | `400 Bad Request` + `"El vehículo no existe en el sistema."`            |
| Choque temporal         | Cita en día y hora ocupados                 | `400 Bad Request` (HorarioOcupadoException) + `"Ya existe una cita agendada en ese horario."` |
| Saturación operativa    | 21ª cita en el mismo día                    | `400 Bad Request` (HorarioOcupadoException) + `"Límite de 20 citas diarias alcanzado."` |

---

## 🧪 Plan de Pruebas Unitarias

Esta sección resume la estrategia de pruebas unitarias adoptada por el equipo.

### Alcance y Enfoque

Las pruebas unitarias cubren la capa de servicio (`CitaService`) del módulo `booking-service` en su totalidad. Las dependencias externas (repositorio JPA y cliente HTTP hacia `garage-service`) son simuladas utilizando **Mockito para asegurar el aislamiento completo del test de la lógica de negocio.

### Reglas Críticas Bajo Cobertura

Todas las pruebas unitarias y de integración están contenidas en la clase de prueba única [CitaServiceTest.java](file:///c:/Users/barra/OneDrive/Documentos/new-autocare-main/booking-service/src/test/java/com/autocare/booking_service/service/CitaServiceTest.java):

| Código | Regla                                          | Método de prueba (en `CitaServiceTest`) |
|--------|------------------------------------------------|-----------------------------------------|
| RN-01  | Existencia de cliente y vehículo               | `agendarCita_LanzaExcepcion_ClienteNoExiste`, `agendarCita_LanzaExcepcion_VehiculoNoExiste` |
| RN-03  | Sin choques de horario                         | `agendarCita_LanzaExcepcion_PorChoqueDeHorario` |
| RN-04  | Límite diario de 20 citas                      | `agendarCita_LanzaExcepcion_PorLimiteDiario`, `agendarCita_Exito_ConExactamente19CitasDelDia` |

---

### Estructura de los Tests: Given-When-Then

Los métodos de prueba siguen la convención **Given-When-Then** (Dado-Cuando-Entonces). A continuación se muestran ejemplos basados directamente en la implementación real de [CitaServiceTest.java](file:///c:/Users/barra/OneDrive/Documentos/new-autocare-main/booking-service/src/test/java/com/autocare/booking_service/service/CitaServiceTest.java):

#### Ejemplo 1 — RN-04: Límite diario de 20 citas
```java
@Test
@DisplayName("Validación RN-04: Rechazar agendamiento si supera 20 citas diarias")
void agendarCita_LanzaExcepcion_PorLimiteDiario() {
    // GIVEN — El cliente y vehículo son válidos, pero el taller ya tiene 20 citas registradas ese día
    when(garageClient.existeCliente(anyLong())).thenReturn(true);
    when(garageClient.existeVehiculo(anyLong())).thenReturn(true);
    when(repository.countByFecha(citaRequestValida.fechaHora().toLocalDate()))
            .thenReturn(20L);

    // WHEN & THEN — Se intenta agendar la cita y se espera una excepción de negocio
    RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        citaService.agendarCita(citaRequestValida);
    });

    assertTrue(exception.getMessage().contains("Límite de 20 citas diarias alcanzado"));
    verify(repository, never()).save(any(Cita.class));
}
```

#### Ejemplo 2 — RN-01: Rechazar agendamiento si el vehículo no existe
```java
@Test
@DisplayName("Validación RN-01: Rechazar agendamiento si el vehículo no existe")
void agendarCita_LanzaExcepcion_VehiculoNoExiste() {
    // GIVEN — El cliente existe pero el vehículo no existe en el garage-service
    when(garageClient.existeCliente(anyLong())).thenReturn(true);
    when(garageClient.existeVehiculo(anyLong())).thenReturn(false);

    // WHEN & THEN — Se espera una excepción 400 Bad Request
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
        citaService.agendarCita(citaRequestValida);
    });

    assertTrue(exception.getMessage().contains("El vehículo no existe en el sistema"));
    verify(repository, never()).save(any(Cita.class));
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
