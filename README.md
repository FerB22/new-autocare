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

| Servicio                | Puerto | Descripción                                      |
|-------------------------|--------|--------------------------------------------------|
| `eureka-server`         | 8761   | Registro y descubrimiento de servicios           |
| `api-gateway`           | 8080   | Puerta de entrada única y enrutamiento           |
| `garage-service`        | 8081   | Gestión unificada de clientes y vehículos        |
| `booking-service`       | 8082   | Agendamiento de citas y reservas                 |
| `loyalty-service`       | 8083   | Sistema de puntos, niveles y recompensas         |
| `workshop-service`      | 8084   | Núcleo de operaciones y órdenes de trabajo       |
| `diagnostics-service`   | 8085   | Historial clínico, telemetría y códigos OBD2     |
| `hr-service`            | 8086   | Gestión del personal y disponibilidad de mecánicos |
| `inventory-service`     | 8087   | Control de stock y catálogo de repuestos         |
| `analytics-service`     | 8088   | Observatorio de métricas y reportes financieros  |
| `billing-service`       | 8089   | Emisión de facturas y control de pagos           |
| `procurement-service`   | 8090   | Gestión de proveedores y órdenes de compra       |

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
