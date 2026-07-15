# Guía de Pruebas con Postman: Garage, Booking y Loyalty Services

Este documento detalla los pasos para realizar las pruebas de integración y validación de los microservicios **Garage-service** (Clientes y Vehículos), **Booking-service** (Agendamiento de Citas) y **Loyalty-service** (Módulo de Lealtad) utilizando Postman o cualquier cliente REST.

---

## 1. Configuración de Entorno y Direcciones Base

Para ejecutar estas pruebas, asegúrate de tener activos el servidor de descubrimiento **Eureka Server**, el **API Gateway** y los microservicios correspondientes.

Puedes realizar las peticiones de dos maneras:

### A. A través del API Gateway (Recomendado para simular entorno real)
El API Gateway centraliza todas las llamadas en el puerto **8080** y las redirige al microservicio correspondiente:
*   **Base URL:** `http://localhost:8080`
*   **Ruta Garage-service:** `/api/garage/**`
*   **Ruta Booking-service:** `/api/reservas/**`
*   **Ruta Loyalty-service:** `/api/lealtad/**`

### B. Conexión Directa a cada Microservicio
Si deseas saltarte el Gateway para pruebas unitarias de red:
*   **Garage-service Base URL:** `http://localhost:8081` (Ruta: `/api/garage/clientes`)
*   **Booking-service Base URL:** `http://localhost:8085` (Ruta: `/api/reservas/citas`)
*   **Loyalty-service Base URL:** `http://localhost:8088` (Ruta: `/api/lealtad/cliente`)

> [!NOTE]
> En los ejemplos siguientes utilizaremos la ruta a través del **API Gateway (`http://localhost:8080`)**. Si pruebas de forma directa, cambia el puerto a `8081`, `8085` o `8088` según corresponda y mantén el path respectivo.

---

## 2. Pruebas para Garage-service (Clientes y Vehículos)

Este microservicio gestiona los datos de los clientes y sus vehículos.

### Paso 2.1: Crear un Cliente Exitosamente (POST)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/garage/clientes`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body -> raw -> JSON):**
    ```json
    {
      "documentoIdentidad": "12345678-9",
      "nombre": "Juan",
      "apellido": "Pérez",
      "email": "juan.perez@example.com",
      "telefono": "+56912345678"
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `201 Created`
    *   **Respuesta JSON:** Retorna el cliente creado con un `id` asignado automáticamente (ej. `1`).

### Paso 2.2: Intentar Crear un Cliente Inválido (Prueba de Validación - 400 Bad Request)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/garage/clientes`
*   **Cuerpo (Body - Email incorrecto y campos nulos):**
    ```json
    {
      "documentoIdentidad": "",
      "nombre": "Juan",
      "apellido": "",
      "email": "correo-invalido",
      "telefono": "+56912345678"
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `400 Bad Request`
    *   **Respuesta JSON:** Detalle de los errores de validación (`documentoIdentidad` no puede estar en blanco, `email` debe ser una dirección de correo válida).

### Paso 2.3: Listar Clientes Registrados (GET)
*   **Método:** `GET`
*   **URL:** `http://localhost:8080/api/garage/clientes`
*   **Resultado Esperado:**
    *   **Código HTTP:** `200 OK`
    *   **Respuesta JSON:** Un arreglo con todos los clientes creados en la base de datos.

### Paso 2.4: Asociar un Vehículo al Cliente (POST)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/garage/clientes/1/vehiculos` *(Reemplaza `1` por el ID del cliente creado)*
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body -> raw -> JSON):**
    ```json
    {
      "patente": "ABCD-12",
      "marca": "Toyota",
      "modelo": "RAV4",
      "anio": 2022,
      "color": "Gris",
      "vin": "93847291038475618"
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `201 Created`
    *   **Respuesta JSON:** El vehículo registrado y vinculado correctamente al cliente.

---

## 3. Pruebas para Booking-service (Agendamiento de Citas)

Este microservicio se encarga de programar las citas del taller, aplicando reglas como evitar choques horarios, respetar la capacidad máxima del taller y proveer hipermedios HATEOAS.

### Paso 3.1: Agendar una Cita Exitosamente (POST con HATEOAS)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/reservas/citas`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body -> raw -> JSON):**
    > [!IMPORTANT]
    > La fecha y hora (`fechaHora`) deben estar estrictamente en el futuro para pasar la validación `@Future`.
    ```json
    {
      "clienteId": 1,
      "vehiculoId": 1,
      "fechaHora": "2026-12-01T10:00:00",
      "motivo": "Mantención de los 30.000 km y revisión de frenos"
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `201 Created`
    *   **Respuesta JSON:** El recurso de la cita devuelto con la información guardada y una sección de enlaces HATEOAS (`_links`), permitiendo la navegación hacia el recurso individual, la lista completa o su actualización.

### Paso 3.2: Intentar Agendar una Cita en una Fecha Pasada (400 Bad Request)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/reservas/citas`
*   **Cuerpo (Body - Fecha en el pasado):**
    ```json
    {
      "clienteId": 1,
      "vehiculoId": 1,
      "fechaHora": "2020-01-01T10:00:00",
      "motivo": "Cambio de aceite"
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `400 Bad Request`
    *   **Respuesta JSON:** Detalle del error indicando que la fecha debe estar en el futuro.

### Paso 3.3: Obtener una Cita por ID (GET)
*   **Método:** `GET`
*   **URL:** `http://localhost:8080/api/reservas/citas/1` *(Reemplaza `1` por el ID de la cita creada)*
*   **Resultado Esperado:**
    *   **Código HTTP:** `200 OK`
    *   **Respuesta JSON:** Detalle de la cita con HATEOAS. Si pides un ID inexistente (ej. `999`), obtendrás un `404 Not Found`.

### Paso 3.4: Cancelar/Eliminar una Cita (DELETE)
*   **Método:** `DELETE`
*   **URL:** `http://localhost:8080/api/reservas/citas/1`
*   **Resultado Esperado:**
    *   **Código HTTP:** `204 No Content`
    *   **Respuesta JSON:** Sin contenido en el cuerpo (vacío), confirmando la eliminación del recurso.

---

## 4. Pruebas para Loyalty-service (Módulo de Lealtad)

Este microservicio se encarga de gestionar los perfiles de lealtad de los clientes, la acumulación de puntos y su canje con control de reglas de negocio (ej. saldo mínimo).

### Paso 4.1: Crear un Perfil de Lealtad (POST)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/lealtad/cliente`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body -> raw -> JSON):**
    ```json
    {
      "clienteId": 1
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `201 Created`
    *   **Respuesta JSON:** Retorna el perfil de lealtad creado con `clienteId: 1`, saldo de puntos acumulados inicializado en `0`, nivel `BRONCE` y enlaces HATEOAS (`_links`).

### Paso 4.2: Intentar Crear Perfil con ID de Cliente Inválido (400 Bad Request)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/lealtad/cliente`
*   **Cuerpo (Body - ID inválido):**
    ```json
    {
      "clienteId": -5
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `400 Bad Request`
    *   **Respuesta JSON:** Mensaje indicando que el ID del cliente debe ser un número positivo.

### Paso 4.3: Sumar Puntos a un Perfil (POST)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/lealtad/cliente/1/sumar`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body -> raw -> JSON):**
    ```json
    {
      "cantidadPuntos": 150
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `200 OK`
    *   **Respuesta JSON:** El perfil actualizado mostrando `puntosAcumulados: 150`, nivel calculado (ej. `BRONCE`), y enlaces HATEOAS actualizados.

### Paso 4.4: Canjear Puntos Exitosamente (POST)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/lealtad/cliente/1/canjear`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body -> raw -> JSON):**
    ```json
    {
      "cantidadPuntos": 50
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `200 OK`
    *   **Respuesta JSON:** El perfil de lealtad actualizado con `puntosAcumulados: 100`.

### Paso 4.5: Intentar Canjear Más Puntos de los Disponibles (400 Bad Request)
*   **Método:** `POST`
*   **URL:** `http://localhost:8080/api/lealtad/cliente/1/canjear`
*   **Headers:**
    *   `Content-Type: application/json`
*   **Cuerpo (Body - Intento de sobregiro):**
    ```json
    {
      "cantidadPuntos": 500
    }
    ```
*   **Resultado Esperado:**
    *   **Código HTTP:** `400 Bad Request`
    *   **Respuesta JSON:** Mensaje de error de negocio indicando saldo insuficiente o similar.

### Paso 4.6: Consultar Perfil de Lealtad (GET)
*   **Método:** `GET`
*   **URL:** `http://localhost:8080/api/lealtad/cliente/1`
*   **Resultado Esperado:**
    *   **Código HTTP:** `200 OK`
    *   **Respuesta JSON:** El perfil de lealtad completo del cliente con su nivel y saldo actual.

---

## 5. Guía de Códigos de Estado HTTP (El significado de los números)

En la defensa de tu proyecto, los evaluadores querrán ver si entiendes la semántica de la web y del protocolo HTTP. Aquí te explicamos qué significan los números que Postman devuelve:

| Código HTTP | Nombre Standard | Significado Práctico | Justificación para tu Defensa |
| :---: | :--- | :--- | :--- |
| **`200 OK`** | OK | La solicitud se completó con éxito y el servidor retorna la información solicitada en el cuerpo. | Se utiliza para lecturas exitosas (`GET`) o actualizaciones de recursos existentes (`PUT`). |
| **`201 Created`** | Created | La solicitud tuvo éxito y, como resultado, se ha creado un nuevo recurso en el servidor. | Se utiliza específicamente en peticiones `POST` de creación (ej. crear cliente, registrar vehículo, agendar cita) para indicar que el recurso se guardó en la base de datos. |
| **`204 No Content`** | No Content | La petición se procesó con éxito, pero no es necesario retornar ningún cuerpo en la respuesta. | Típico de operaciones `DELETE` exitosas o actualizaciones donde el cliente no requiere recibir el recurso modificado. |
| **`400 Bad Request`** | Bad Request | El servidor no pudo procesar la solicitud debido a un error del cliente (sintaxis inválida, parámetros incorrectos, fallas de validación). | Se devuelve cuando los datos enviados no cumplen las restricciones (ej. campos vacíos con `@NotBlank`, fechas pasadas con `@Future`, correos mal formados, o reglas de negocio violadas como colisión de horarios). |
| **`404 Not Found`** | Not Found | El servidor no puede encontrar el recurso solicitado. | Se utiliza cuando consultas o intentas modificar un recurso por un ID que no existe en la base de datos (ej. un cliente o una cita inexistente). |
| **`500 Internal Server Error`** | Internal Server Error | El servidor encontró una condición inesperada que le impidió completar la solicitud. | Representa un error del lado del servidor (código Java que falló por un `NullPointerException`, pérdida de conexión con la base de datos, etc.). **En producción no debe ocurir y se debe capturar.** |

---

## 6. Tips de Defensa: Cómo justificar tus respuestas HTTP

1.  **Diferencia entre 400 (Bad Request) y 500 (Internal Server Error):**
    *   *Defensa:* "Un error de validación (como mandar un correo mal escrito o una fecha en el pasado) es responsabilidad del cliente que envía la petición. Por lo tanto, el protocolo HTTP dicta que debe responderse con la familia `4xx` (específicamente **`400 Bad Request`**). Responder con un **`500`** sería incorrecto porque la aplicación no falló internamente; la aplicación capturó la entrada errónea preventivamente utilizando `@Valid` y respondió de acuerdo al estándar."
2.  **Diferencia entre 200 (OK) y 201 (Created):**
    *   *Defensa:* "Aunque ambos indican éxito (`2xx`), el código **`201 Created`** es semánticamente más preciso para operaciones de persistencia (`POST`), ya que informa explícitamente al cliente que un nuevo recurso fue registrado e incluso puede retornar el URI del nuevo elemento en los headers de respuesta."
3.  **Uso de HATEOAS (Hypermedia As The Engine Of Application State):**
    *   *Defensa:* "Al retornar enlaces en la respuesta del microservicio de citas, estamos desacoplando al frontend de las URIs cableadas (hardcoded). El cliente web o móvil solo necesita conocer la dirección inicial y luego puede navegar e interactuar de forma dinámica consumiendo las direcciones que la misma API le ofrece."
