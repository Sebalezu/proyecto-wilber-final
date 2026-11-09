# 03 - Especificación de Casos de Uso del Sistema
**Proyecto:** La Dorada S.A.S. – Sistema de Pedidos e Inventario  
**Versión:** 1.0.0  
**Fecha:** 2026-11-09  

---

## 1. Actores del Sistema

| Actor | Descripción | Responsabilidades Clave |
| :--- | :--- | :--- |
| **Administrador** | Usuario con control total administrativo y gerencial. | Configuración de catálogo, creación/edición/desactivación de productos, confirmación y cancelación de pedidos, despacho y auditoría general. |
| **Vendedor** | Usuario encargado de la relación comercial y clientes. | Consulta de catálogo y stock, creación y registro de pedidos, confirmación de pedidos pendientes, rechazo/cancelación previa. |
| **Bodega** | Usuario responsable del almacén, existencias físicas y logística. | Consulta de existencias y alertas de stock bajo, ingreso de mercancía / ajuste de existencias, despacho de pedidos y cancelación de envíos confirmados con restitución de inventario. |
| **Sistema** | Mecanismo automático de persistencia y reglas de negocio. | Control de concurrencia, bloqueo pesimista en base de datos, cálculo automático de totales, restitución/descuento transaccional y generación de consecutivos. |

---

## 2. Matriz de Casos de Uso vs. Roles (Permisos)

| Código | Caso de Uso | Administrador | Vendedor | Bodega |
| :--- | :--- | :---: | :---: | :---: |
| **CU-01** | Iniciar Sesión (Autenticación) | ✔ | ✔ | ✔ |
| **CU-02** | Cerrar Sesión | ✔ | ✔ | ✔ |
| **CU-03** | Consultar Catálogo y Existencias | ✔ | ✔ | ✔ |
| **CU-04** | Registrar Nuevo Producto | ✔ | ✖ | ✖ |
| **CU-05** | Modificar Producto (Precio / Datos) | ✔ | ✖ | ✖ |
| **CU-06** | Activar / Desactivar Producto | ✔ | ✖ | ✖ |
| **CU-07** | Registrar / Consultar Clientes | ✔ | ✔ | ✔ |
| **CU-08** | Monitorear Alertas de Stock Bajo | ✔ | ✔ | ✔ |
| **CU-09** | Registrar Ajuste de Stock (Ingreso de Mercancía) | ✔ | ✖ | ✔ |
| **CU-10** | Consultar Historial de Movimientos (Kardex) | ✔ | ✔ | ✔ |
| **CU-11** | Registrar Nuevo Pedido | ✔ | ✔ | ✖ |
| **CU-12** | Consultar y Filtrar Listado de Pedidos | ✔ | ✔ | ✔ |
| **CU-13** | Visualizar Detalle y Trazabilidad de Pedido | ✔ | ✔ | ✔ |
| **CU-14** | Modificar Líneas de Pedido Pendiente | ✔ | ✔ | ✖ |
| **CU-15** | Confirmar Pedido (Descuento de Stock en 2 Fases) | ✔ | ✔ | ✖ |
| **CU-16** | Despachar Pedido Confirmado | ✔ | ✖ | ✔ |
| **CU-17** | Cancelar Pedido Confirmado (con Restitución) | ✔ | ✖ | ✔ |
| **CU-18** | Cancelar o Rechazar Pedido Pendiente | ✔ | ✔ | ✖ |

---

## 3. Especificación Detallada de Casos de Uso

### MÓDULO 1: AUTENTICACIÓN Y ACCESO

#### CU-01: Iniciar Sesión
- **Actores:** Administrador, Vendedor, Bodega.
- **Precondición:** El usuario debe estar registrado y con `activo = true` en la tabla `usuario`.
- **Flujo Principal:**
  1. El usuario accede a la ruta `/login`.
  2. El sistema presenta el formulario de correo/contraseña y el selector rápido de usuarios de prueba.
  3. El usuario ingresa sus credenciales (o selecciona un usuario de prueba) y envía la solicitud.
  4. El sistema valida las credenciales contra `POST /api/auth/login`.
  5. El sistema responde con el perfil (`id`, `nombre`, `email`, `rol`) y almacena la sesión en el cliente.
  6. El sistema redirige al usuario a la vista de `/inicio` (o a la ruta previa solicitada).
- **Flujos Alternativos:**
  - *4a. Credenciales incorrectas o usuario inactivo:* El sistema responde HTTP 401 Unauthorized y la interfaz muestra una alerta de error sin ingresar al sistema.
- **Postcondición:** Sesión activa con menú y accesos restringidos según el rol asignado.

#### CU-02: Cerrar Sesión
- **Actores:** Administrador, Vendedor, Bodega.
- **Precondición:** El usuario tiene una sesión activa.
- **Flujo Principal:**
  1. El usuario hace clic en el botón "Cerrar sesión" en el encabezado.
  2. El sistema elimina las credenciales del almacenamiento local y limpia el estado reactivo.
  3. El sistema redirige automáticamente a la pantalla de `/login`.
- **Postcondición:** La sesión se destruye y las rutas protegidas quedan bloqueadas por `AuthGuard`.

---

### MÓDULO 2: CATÁLOGO Y CLIENTES

#### CU-03: Consultar Catálogo de Productos
- **Actores:** Administrador, Vendedor, Bodega.
- **Precondición:** Usuario autenticado.
- **Flujo Principal:**
  1. El usuario navega a `/catalogo`.
  2. El sistema consulta `GET /api/productos` con los filtros aplicados (búsqueda por texto `q` y estado `activo`).
  3. El sistema presenta el listado con código, nombre, precio en pesos colombianos ($ COP), existencias actuales y badges de nivel de stock.
- **Postcondición:** Información de productos visualizada en tiempo real.

#### CU-04: Registrar Nuevo Producto
- **Actores:** Administrador.
- **Precondición:** Usuario con rol `ADMINISTRADOR`.
- **Flujo Principal:**
  1. El administrador presiona "Registrar Nuevo Producto" en `/catalogo`.
  2. Ingresa nombre, precio unitario (> 0), existencias iniciales (>= 0) y opcionalmente un código.
  3. Si no se suministra código, el sistema calcula el siguiente consecutivo automático con formato `ASE-001`, `ASE-002`, etc.
  4. El sistema valida la unicidad del código y guarda el registro en la tabla `producto`.
  5. Se actualiza el catálogo y se muestra mensaje de confirmación.
- **Flujos Alternativos:**
  - *4a. Código duplicado:* La API responde HTTP 409 Conflict y el formulario alerta que el código ya existe.

#### CU-05: Modificar Producto
- **Actores:** Administrador.
- **Precondición:** Producto existente y rol `ADMINISTRADOR`.
- **Flujo Principal:**
  1. El administrador hace clic en "Editar" sobre un producto.
  2. Modifica el nombre, precio o existencias.
  3. Si las existencias cambian, el sistema genera automáticamente un movimiento de inventario de tipo `AJUSTE` por la diferencia absoluta.
  4. El sistema persiste los cambios vía `PUT /api/productos/{id}` y refresca el catálogo.

#### CU-06: Activar / Desactivar Producto
- **Actores:** Administrador.
- **Precondición:** Rol `ADMINISTRADOR`.
- **Flujo Principal:**
  1. El administrador presiona el botón "Activar" o "Desactivar" sobre la fila del producto.
  2. El sistema envía `PATCH /api/productos/{id}/activo`.
  3. Los productos inactivos dejan de aparecer como seleccionables en la creación de nuevos pedidos.

#### CU-07: Registrar y Consultar Clientes
- **Actores:** Administrador, Vendedor.
- **Precondición:** Usuario autenticado.
- **Flujo Principal:**
  1. Durante la creación de un pedido o consulta general, el usuario visualiza los clientes disponibles (`GET /api/clientes`).
  2. Si el cliente no existe, se abre el modal rápido de creación (`POST /api/clientes`) ingresando nombre y teléfono.
  3. El cliente se asocia de inmediato al pedido en curso.

---

### MÓDULO 3: GESTIÓN DE INVENTARIO Y STOCK

#### CU-08: Monitorear Alertas de Stock Bajo
- **Actores:** Administrador, Vendedor, Bodega.
- **Precondición:** Usuario autenticado.
- **Flujo Principal:**
  1. El usuario accede a `/inicio`.
  2. El sistema consulta `GET /api/inventario/stock-bajo` identificando productos con existencias $\le 10$ unidades (o agotados con 0 unidades).
  3. La interfaz categoriza la severidad:
     - 🔴 **Crítico / Agotado:** Existencias $\le 5$ unidades.
     - 🟡 **Advertencia:** Existencias entre 6 y 10 unidades.
  4. El usuario puede saltar directamente al catálogo o tomar medidas de reabastecimiento.

#### CU-09: Registrar Ajuste de Stock (Ingreso de Mercancía)
- **Actores:** Administrador, Bodega.
- **Precondición:** Rol `ADMINISTRADOR` o `BODEGA`.
- **Flujo Principal:**
  1. En `/catalogo`, el usuario de Bodega o Admin pulsa "+ Stock" en el producto deseado.
  2. Se despliega el modal de ingreso físico indicando existencias actuales.
  3. El usuario digita la cantidad adicional a ingresar (entero $>0$) y el motivo (ej. "Llegada de camión proveedor").
  4. El sistema muestra la proyección del nuevo stock (`actual + adicional`).
  5. Se envía la solicitud a `POST /api/inventario/ajuste`.
  6. El backend aplica bloqueo pesimista, suma las unidades a la tabla `producto` y registra la transacción en `movimiento_inventario` como `AJUSTE`.
  7. La tabla de catálogo se actualiza en vivo con las nuevas existencias.

#### CU-10: Consultar Kardex de Movimientos
- **Actores:** Administrador, Vendedor, Bodega.
- **Flujo Principal:**
  1. El sistema permite consultar `GET /api/inventario/movimientos` filtrando por producto, pedido o tipo (`DESCUENTO`, `RESTITUCION`, `AJUSTE`).
  2. Permite auditar el motivo y momento exacto de cada fluctuación de inventario.

---

### MÓDULO 4: CICLO DE VIDA DE PEDIDOS

```
                    ┌──────────────┐
                    │  PENDIENTE   │
                    └──────┬───────┘
           ┌───────────────┼───────────────┐
           │ (Confirmar)   │ (Cancelar)    │ (Rechazar)
           ▼               ▼               ▼
    ┌──────────────┐┌──────────────┐┌──────────────┐
    │  CONFIRMADO  ││  CANCELADO   ││  RECHAZADO   │
    │(Stock Descont)││ (Sin Stock)  ││ (Sin Stock)  │
    └──────┬───────┘└──────────────┘└──────────────┘
           ├───────────────┐
           │ (Despachar)   │ (Cancelar con Restitución)
           ▼               ▼
    ┌──────────────┐┌──────────────┐
    │  DESPACHADO  ││  CANCELADO   │
    │ (Completado) ││ (Stock Rest) │
    └──────────────┘└──────────────┘
```

#### CU-11: Registrar Nuevo Pedido
- **Actores:** Administrador, Vendedor.
- **Precondición:** Existencia de al menos un cliente y productos activos en catálogo.
- **Flujo Principal:**
  1. El usuario ingresa a `/nuevo-pedido`.
  2. Selecciona el cliente destino.
  3. Agrega una o más líneas indicando el producto y la cantidad requerida ($>0$).
  4. La interfaz recalcula dinámicamente los subtotales y el total estimado.
  5. El usuario elige una de dos opciones de guardado:
     - **Opción A (Guardar como Pendiente):** Envía `confirmar: false`. El pedido se crea en estado `PENDIENTE` sin reservar inventario.
     - **Opción B (Confirmar Pedido Directo):** Envía `confirmar: true`. El sistema ejecuta la validación previa en 2 fases con bloqueo pesimista; si hay existencias, descuenta el stock de inmediato y nace en estado `CONFIRMADO`.
  6. El sistema redirige a `/pedidos/{id}` mostrando el detalle generado.
- **Flujos Alternativos:**
  - *5b. Stock Insuficiente en Confirmación:* El backend responde HTTP 409 Conflict con el listado detallado de faltantes. La interfaz resalta un contenedor de alerta rojo (`#FDE5E3`) indicando: *"Producto X: pediste N y hay M disponibles"*. La orden no se crea y no se descuenta ninguna existencia.

#### CU-12: Consultar y Filtrar Pedidos
- **Actores:** Administrador, Vendedor, Bodega.
- **Flujo Principal:**
  1. El usuario accede a `/pedidos`.
  2. Puede filtrar por estado mediante píldoras interactivas (`TODOS`, `PENDIENTE`, `CONFIRMADO`, `DESPACHADO`, `RECHAZADO`, `CANCELADO`) o por cliente y texto de búsqueda.
  3. En pantalla de escritorio visualiza la tabla interactiva; en dispositivos móviles visualiza tarjetas responsivas optimizadas.
  4. Al hacer clic sobre cualquier orden, se navega al detalle `/pedidos/:id`.

#### CU-13: Visualizar Detalle y Trazabilidad de Pedido
- **Actores:** Administrador, Vendedor, Bodega.
- **Flujo Principal:**
  1. El usuario ingresa a `/pedidos/:id`.
  2. Visualiza la información del cliente, fecha de registro y monto total en $ COP.
  3. Observa el **Stepper / Línea de tiempo visual** con el progreso del ciclo de vida (`PENDIENTE` $\rightarrow$ `CONFIRMADO` $\rightarrow$ `DESPACHADO`).
  4. Consulta la tabla de líneas del pedido y el indicador de si el stock ya fue descontado.
  5. Examina la tabla de **Auditoría e Historial Cronológico** con las marcas de tiempo y transiciones de estado previas.

#### CU-14: Modificar Líneas de Pedido Pendiente
- **Actores:** Administrador, Vendedor.
- **Precondición:** El pedido debe encontrarse estrictamente en estado `PENDIENTE`.
- **Flujo Principal:**
  1. En el detalle del pedido, el usuario presiona "Modificar Líneas".
  2. El sistema activa el modo de edición permitiendo sumar líneas, quitar productos o cambiar cantidades.
  3. Se recalcula en vivo el total estimado.
  4. El usuario presiona "Guardar Cambios" (`PUT /api/pedidos/{id}`).
  5. El backend valida las líneas, recalcula el total exacto y actualiza el pedido.
- **Flujos Alternativos:**
  - *Intentar modificar un pedido no pendiente:* El backend responde HTTP 409 Conflict (`PedidoNoEditableException`) impidiendo cualquier alteración si la orden ya fue confirmada o cerrada.

#### CU-15: Confirmar Pedido Pendiente
- **Actores:** Administrador, Vendedor.
- **Precondición:** Pedido en estado `PENDIENTE`.
- **Flujo Principal:**
  1. El usuario pulsa "Confirmar Pedido".
  2. Confirma la acción en el cuadro de diálogo.
  3. El sistema envía `PATCH /api/pedidos/{id}/estado` con `{"estado": "CONFIRMADO"}`.
  4. El backend ejecuta la validación atómica en 2 fases:
     - **Fase 1:** Agrupa cantidades por producto, aplica bloqueo pesimista en base de datos (`SELECT FOR UPDATE`) y comprueba el stock disponible de todos los ítems.
     - **Fase 2:** Si todo alcanza, descuenta el stock de cada producto y registra un movimiento `DESCUENTO` en `movimiento_inventario`.
  5. El pedido cambia a `CONFIRMADO`, se marca `stockDescontado = true` y se anexa la entrada en el historial de estados.
- **Flujos Alternativos:**
  - *Stock Insuficiente:* Se lanza `StockInsuficienteException` (HTTP 409). No se modifica ninguna existencia (rollback total). La interfaz muestra el banner rojo `#FDE5E3` detallando exactamente el déficit por producto.

#### CU-16: Despachar Pedido Confirmado
- **Actores:** Administrador, Bodega.
- **Precondición:** Pedido en estado `CONFIRMADO`.
- **Flujo Principal:**
  1. El personal de bodega o administración presiona "Despachar Pedido".
  2. Confirma que la mercancía ha sido embalada y enviada al cliente.
  3. El sistema envía `PATCH /api/pedidos/{id}/estado` con `{"estado": "DESPACHADO"}`.
  4. El pedido pasa a estado terminal `DESPACHADO` (no requiere nuevos movimientos de inventario porque el stock ya fue descontado en la confirmación).
  5. La orden queda cerrada de forma irreversible.

#### CU-17: Cancelar Pedido Confirmado (con Restitución de Stock)
- **Actores:** Administrador, Bodega.
- **Precondición:** Pedido en estado `CONFIRMADO`.
- **Flujo Principal:**
  1. El usuario presiona "Cancelar Pedido (Restituye Stock)".
  2. El sistema muestra el mensaje de confirmación de alto impacto:
     > *"¿Desea cancelar el pedido? El inventario reservado será restituido a bodega automáticamente."*
  3. Al confirmar, el backend transiciona a `CANCELADO`.
  4. Al detectar que `stockDescontado` era `true`, el servicio `InventarioService.restituir` devuelve automáticamente las unidades a las existencias de cada producto y genera movimientos de auditoría tipo `RESTITUCION`.
  5. La interfaz pasa a mostrar el banner terminal de pedido cancelado y el stock queda disponible de inmediato para otros pedidos.

#### CU-18: Cancelar o Rechazar Pedido Pendiente
- **Actores:** Administrador, Vendedor.
- **Precondición:** Pedido en estado `PENDIENTE`.
- **Flujo Principal:**
  1. El usuario presiona "Rechazar Pedido" o "Cancelar Pedido".
  2. Confirma la acción.
  3. El pedido pasa a `RECHAZADO` o `CANCELADO`. Dado que en estado pendiente nunca se reservó stock, no se altera el inventario físico ni se generan movimientos en el Kardex.
