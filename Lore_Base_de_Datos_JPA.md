# Guía de Estudio: El Lore de tu Base de Datos (Modelado y JPA)
## DSY1103 - Desarrollo FullStack 1 (Examen Final Transversal)

Esta guía explica detalladamente cómo están modelados los datos en **AutoCare**, cómo se estructuran las relaciones físicas internas (JPA) y cómo se manejan las relaciones lógicas entre microservicios. Está diseñada para que obtengas el **100% de logro en el indicador IE 2.1.3 e IE 2.1.1** de la defensa técnica.

---

## 1. El Concepto Clave: *Database-per-Service* (Base de Datos por Servicio)

En una arquitectura de microservicios **no existe una única base de datos gigante**. Cada microservicio es dueño absoluto de su propio esquema y almacenamiento de datos (*Bounded Context*). Ningún servicio puede consultar la base de datos de otro directamente.

Debido a esto, las relaciones se clasifican en dos tipos:
1. **Relaciones Físicas (Internas)**: Mapeadas directamente por JPA/Hibernate en la misma base de datos del servicio.
2. **Relaciones Lógicas (Distribuidas)**: Referencias virtuales usando identificadores (`Long`) que se resuelven a través de llamadas de red (API REST) usando `WebClient` o `Feign Client`.

---

## 2. Relaciones Físicas Internas (JPA)

Existen dos lugares principales en tu proyecto donde las entidades conviven en la misma base de datos y utilizan relaciones tradicionales de JPA:

### A. Microservicio: `garage-service`
Este servicio gestiona el núcleo físico del taller: quiénes son los clientes y qué vehículos poseen.
*   **Entidades:** `Cliente` y `Vehiculo`.
*   **Relación:** Un **Cliente** tiene muchos **Vehículos** (`1 a N`).

#### Código en [Cliente.java](file:///c:/Users/barra/OneDrive/Documentos/new-autocare-main/garage-service/src/main/java/com/autocare/garage_service/model/Cliente.java):
```java
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación física 1 a N
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehiculo> vehiculos = new ArrayList<>();
}
```
*   **Explicación para la defensa:** *«En `Cliente` usamos `@OneToMany` apuntando al atributo `cliente` de la clase `Vehiculo`. Declaramos `cascade = CascadeType.ALL` para que al guardar o actualizar un cliente se procesen también sus vehículos de forma automática en cascada, y `orphanRemoval = true` para que si eliminamos un vehículo de la lista, Hibernate lo borre físicamente de la base de datos.»*

#### Código en [Vehiculo.java](file:///c:/Users/barra/OneDrive/Documentos/new-autocare-main/garage-service/src/main/java/com/autocare/garage_service/model/Vehiculo.java):
```java
@Entity
@Table(name = "vehiculos")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación física N a 1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnore // Evita bucle infinito al serializar JSON
    private Cliente cliente;
}
```
*   **Explicación para la defensa:** *«En `Vehiculo` usamos `@ManyToOne` porque muchos vehículos pertenecen a un único cliente. Usamos `fetch = FetchType.LAZY` (carga diferida) por rendimiento, para no traer los datos del cliente a menos que sea estrictamente necesario. Usamos `@JsonIgnore` para prevenir recursión infinita (un bucle sin fin) cuando Jackson intente serializar la relación de forma bidireccional a JSON.»*

---

### B. Microservicio: `procurement-service`
Este servicio gestiona las compras de repuestos a proveedores externos.
*   **Entidades:** `Proveedor` y `OrdenCompra`.
*   **Relación:** Un **Proveedor** puede recibir muchas **Órdenes de Compra** (`1 a N`).

#### Código en [OrdenCompra.java](file:///c:/Users/barra/OneDrive/Documentos/new-autocare-main/procurement-service/src/main/java/com/autocare/procurement_service/model/OrdenCompra.java):
```java
@Entity
@Table(name = "ordenes_compra")
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación física N a 1
    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    // Relación lógica con otro microservicio
    @Column(nullable = false)
    private Long repuestoId; 
}
```
*   **Explicación para la defensa:** *«Una orden de compra tiene una relación física `@ManyToOne` con `Proveedor` dentro de su propia base de datos. Sin embargo, no hay un mapeo JPA con el repuesto; en su lugar, guardamos un campo simple `repuestoId` que representa de forma lógica el repuesto que pertenece al `inventory-service`.»*

---

## 3. Relaciones Lógicas (Distribuidas / Entre Servicios)

Cuando una entidad necesita información de otra entidad que reside en **otro microservicio**, no se usan anotaciones de relación de JPA. Se guarda el ID como un campo básico (`Long`) y la comunicación se realiza mediante servicios REST.

Aquí tienes la lista de relaciones lógicas que debes conocer:

```mermaid
graph TD
    subgraph garage-service [Garage DB]
        C[Cliente] <--- Physical JPA ---> V[Vehículo]
    end

    subgraph booking-service [Booking DB]
        Cita[Cita] -- Logical clienteId --> C
        Cita -- Logical vehiculoId --> V
    end

    subgraph workshop-service [Workshop DB]
        OT[OrdenTrabajo] -- Logical vehiculoId --> V
        OT -- Logical mecanicoId --> M
    end

    subgraph hr-service [HR DB]
        M[Mecánico]
    end

    subgraph loyalty-service [Loyalty DB]
        PL[PerfilLealtad] -- Logical clienteId --> C
    end
    
    subgraph billing-service [Billing DB]
        F[Factura] -- Logical clienteId --> C
    end
```

### Tabla de Relaciones Lógicas clave para explicar:

| Microservicio Origen | Entidad | Atributo Lógico | Hace referencia a... | ¿Cómo se resuelve en código? |
| :--- | :--- | :--- | :--- | :--- |
| `booking-service` | `Cita` | `clienteId` | `Cliente` (`garage-service`) | El controlador llama a `garage-service` por HTTP REST para verificar que el cliente existe antes de agendar. |
| `booking-service` | `Cita` | `vehiculoId` | `Vehiculo` (`garage-service`) | Igualmente, valida que el vehículo exista en el garage antes de agendar la cita. |
| `workshop-service` | `OrdenTrabajo` | `vehiculoId` | `Vehiculo` (`garage-service`) | Permite saber a qué vehículo se le está realizando el mantenimiento. |
| `workshop-service` | `OrdenTrabajo` | `mecanicoId` | `Mecanico` (`hr-service`) | Asocia la orden de trabajo con el especialista que la ejecuta. |
| `loyalty-service` | `PerfilLealtad`| `clienteId` | `Cliente` (`garage-service`) | Mapea de forma lógica un perfil de puntos de lealtad (`1 a 1`) con el cliente. |
| `billing-service` | `Factura` | `clienteId` | `Cliente` (`garage-service`) | Identifica quién debe pagar la factura emitida. |
| `procurement-service`| `OrdenCompra` | `repuestoId` | `Repuesto` (`inventory-service`) | Indica qué ítem del inventario se está solicitando reabastecer al proveedor. |

---

## 4. Guión para Defender tu Lore en la Evaluación (Tus 15 Minutos)

Si el docente te pregunta: **«Explícame el modelado de datos y cómo se relacionan las entidades de tu proyecto»**, debes responder con confianza usando esta estructura:

> **Paso 1: Establecer la regla de juego (Microservicios)**
> *«Profesor, dado que estamos en una arquitectura de microservicios, aplicamos el patrón **Database-per-Service**. Por lo tanto, no tenemos una base de datos centralizada con foreign keys tradicionales para todo. Dividimos el diseño en relaciones físicas internas y relaciones lógicas distribuidas.»*
>
> **Paso 2: Mostrar tus relaciones JPA físicas (Garage)**
> *«En el microservicio **garage-service** tenemos nuestras relaciones físicas principales usando JPA. La entidad `Cliente` tiene una relación de uno a muchos (`@OneToMany`) con `Vehiculo`. Por su parte, la entidad `Vehiculo` tiene un `@ManyToOne` apuntando a `Cliente`. Esta relación está configurada como `LAZY` para optimizar el rendimiento y tiene `@JsonIgnore` para evitar bucles infinitos durante la conversión a JSON. También definimos cascadas completas (`CascadeType.ALL`) para que los vehículos dependan del ciclo de vida de su dueño.»*
>
> **Paso 3: Explicar cómo se conectan los microservicios (Relación Lógica)**
> *«Para el resto del ecosistema de AutoCare, usamos **relaciones lógicas**. Por ejemplo, en el microservicio **booking-service**, la entidad `Cita` no tiene un `@ManyToOne` clásico a Cliente. En su lugar, almacena un atributo simple `clienteId` y un `vehiculoId`. Cuando necesitamos obtener o validar esta información, nos comunicamos de forma reactiva con el microservicio **garage-service** a través de la red, garantizando así el desacoplamiento total de las bases de datos.»*
