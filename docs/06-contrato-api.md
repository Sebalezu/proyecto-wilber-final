# 06 - Contrato de API: Catálogo e Inventario

Documentación técnica del contrato de la API REST para los módulos de **Catálogo (Productos y Clientes)** e **Inventario**, así como el contrato del servicio interno `InventarioService` destinado al módulo de Pedidos (Persona 3).

---

## 1. Convenciones Generales

- **URL Base:** `http://localhost:8080/api`
- **Formato de intercambio:** `application/json;charset=UTF-8`
- **CORS:** Habilitado para `http://localhost:4200` (métodos `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`).
- **Nombres de campos:** camelCase en español (`codigo`, `nombre`, `precio`, `existencias`, `activo`, `stockBajo`, `productoId`, `pedidoId`, `tipo`).
- **Documentación Interactiva:** Swagger UI en `http://localhost:8080/swagger-ui.html` | OpenAPI JSON en `http://localhost:8080/api-docs`.

---

## 2. Formato Estándar de Errores

En caso de fallo, la API responde con un objeto JSON unificado:

```json
{
  "timestamp": "2026-11-08T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos enviados",
  "path": "/api/productos",
  "errors": [
    {
      "campo": "precio",
      "mensaje": "El precio debe ser mayor a cero"
    }
  ]
}
```

### Códigos de Estado HTTP

| Código | Significado | Escenario de Uso |
| :--- | :--- | :--- |
| `200 OK` | Petición exitosa | Consultas (`GET`) y modificaciones (`PUT`, `PATCH`). |
| `201 Created` | Recurso creado | Creación exitosa de producto o cliente (`POST`). |
| `400 Bad Request` | Petición inválida | Error de validación de campos, valores <= 0, o JSON malformado. |
| `404 Not Found` | Recurso no encontrado | El ID de producto o cliente no existe. |
| `409 Conflict` | Conflicto de negocio | Código de producto duplicado o stock insuficiente para descontar. |
| `500 Internal Error`| Error inesperado | Error no controlado del servidor (sin exponer trazas internas). |

---

## 3. Módulo de Productos (`/api/productos`)

### 3.1 Listar Productos
- **Método y Ruta:** `GET /api/productos`
- **Parámetros Query (opcionales):**
  - `q` (string): Busca coincidencia parcial en nombre o código (sin distinguir mayúsculas/minúsculas).
  - `activo` (boolean): `true` para solo activos, `false` para inactivos. Omitir para listar todos.
- **Códigos de Respuesta:** `200 OK`
- **Ejemplo Response (`200 OK`):**
```json
[
  {
    "id": 1,
    "codigo": "DET-001",
    "nombre": "Detergente Líquido Industrial 5L",
    "precio": 38500.00,
    "existencias": 120,
    "activo": true,
    "stockBajo": false,
    "createdAt": "2026-11-08T10:00:00"
  },
  {
    "id": 2,
    "codigo": "CLO-002",
    "nombre": "Cloro Blanqueador Desinfectante 3.8L",
    "precio": 16800.00,
    "existencias": 8,
    "activo": true,
    "stockBajo": true,
    "createdAt": "2026-11-08T10:00:00"
  }
]
```

### 3.2 Obtener Producto por ID
- **Método y Ruta:** `GET /api/productos/{id}`
- **Códigos de Respuesta:** `200 OK`, `404 Not Found`
- **Ejemplo Response (`200 OK`):**
```json
{
  "id": 1,
  "codigo": "DET-001",
  "nombre": "Detergente Líquido Industrial 5L",
  "precio": 38500.00,
  "existencias": 120,
  "activo": true,
  "stockBajo": false,
  "createdAt": "2026-11-08T10:00:00"
}
```

### 3.3 Crear Producto
- **Método y Ruta:** `POST /api/productos`
- **Validaciones:**
  - `nombre`: Obligatorio, máx. 120 caracteres.
  - `precio`: Obligatorio, mayor a cero (`> 0`).
  - `existencias`: Obligatorio, entero mayor o igual a cero (`>= 0`).
  - `codigo`: Opcional. Si no se envía o viene en blanco, el sistema genera automáticamente el consecutivo `ASE-001`, `ASE-002`, etc. Si se envía, debe ser único.
- **Códigos de Respuesta:** `201 Created`, `400 Bad Request`, `409 Conflict` (código repetido)
- **Ejemplo Request:**
```json
{
  "codigo": "ASE-010",
  "nombre": "Desengrasante Multiusos Cítrico 4L",
  "precio": 24500.00,
  "existencias": 35
}
```
- **Ejemplo Response (`201 Created`):**
```json
{
  "id": 6,
  "codigo": "ASE-010",
  "nombre": "Desengrasante Multiusos Cítrico 4L",
  "precio": 24500.00,
  "existencias": 35,
  "activo": true,
  "stockBajo": false,
  "createdAt": "2026-11-08T16:05:00"
}
```

### 3.4 Actualizar Producto
- **Método y Ruta:** `PUT /api/productos/{id}`
- **Comportamiento de Inventario:** Si las `existencias` enviadas son diferentes a las actuales, el sistema genera y persiste automáticamente un movimiento en `movimiento_inventario` con `tipo = 'AJUSTE'` y `cantidad = |nuevas - anteriores|`.
- **Códigos de Respuesta:** `200 OK`, `400 Bad Request`, `404 Not Found`
- **Ejemplo Request:**
```json
{
  "nombre": "Detergente Líquido Concentrado 5L",
  "precio": 39900.00,
  "existencias": 115
}
```
- **Ejemplo Response (`200 OK`):**
```json
{
  "id": 1,
  "codigo": "DET-001",
  "nombre": "Detergente Líquido Concentrado 5L",
  "precio": 39900.00,
  "existencias": 115,
  "activo": true,
  "stockBajo": false,
  "createdAt": "2026-11-08T10:00:00"
}
```

### 3.5 Activar / Desactivar Producto (Baja Lógica)
- **Método y Ruta:** `PATCH /api/productos/{id}/activo`
- **Nota:** No existe endpoint `DELETE`. Los productos se desactivan para proteger la integridad referencial de pedidos históricos.
- **Códigos de Respuesta:** `200 OK`, `400 Bad Request`, `404 Not Found`
- **Ejemplo Request:**
```json
{
  "activo": false
}
```
- **Ejemplo Response (`200 OK`):**
```json
{
  "id": 1,
  "codigo": "DET-001",
  "nombre": "Detergente Líquido Concentrado 5L",
  "precio": 39900.00,
  "existencias": 115,
  "activo": false,
  "stockBajo": false,
  "createdAt": "2026-11-08T10:00:00"
}
```

---

## 4. Módulo de Clientes (`/api/clientes`)

### 4.1 Listar Clientes
- **Método y Ruta:** `GET /api/clientes`
- **Parámetros Query (opcionales):** `q` (búsqueda por nombre insensible a mayúsculas/minúsculas).
- **Códigos de Respuesta:** `200 OK`
- **Ejemplo Response (`200 OK`):**
```json
[
  {
    "id": 1,
    "nombre": "Restaurante El Sabor Costeño",
    "telefono": "+57 310 555 1234"
  },
  {
    "id": 2,
    "nombre": "Minimarket La Economía",
    "telefono": "+57 315 888 4321"
  }
]
```

### 4.2 Obtener Cliente por ID
- **Método y Ruta:** `GET /api/clientes/{id}`
- **Códigos de Respuesta:** `200 OK`, `404 Not Found`

### 4.3 Crear Cliente
- **Método y Ruta:** `POST /api/clientes`
- **Validaciones:** `nombre` obligatorio (máx. 150), `telefono` obligatorio (máx. 30).
- **Códigos de Respuesta:** `201 Created`, `400 Bad Request`
- **Ejemplo Request:**
```json
{
  "nombre": "Lavandería Express Central",
  "telefono": "+57 320 444 8899"
}
```
- **Ejemplo Response (`201 Created`):**
```json
{
  "id": 5,
  "nombre": "Lavandería Express Central",
  "telefono": "+57 320 444 8899"
}
```

### 4.4 Actualizar Cliente
- **Método y Ruta:** `PUT /api/clientes/{id}`
- **Códigos de Respuesta:** `200 OK`, `400 Bad Request`, `404 Not Found`

---

## 5. Módulo de Inventario (`/api/inventario`)

### 5.1 Consultar Movimientos de Inventario (Kardex)
- **Método y Ruta:** `GET /api/inventario/movimientos`
- **Parámetros Query (opcionales):**
  - `productoId` (long): Filtra por producto específico.
  - `pedidoId` (long): Filtra por pedido asociado.
  - `tipo` (string): `DESCUENTO`, `RESTITUCION` o `AJUSTE`.
- **Ordenamiento:** Descendente por fecha (`ORDER BY fecha DESC`).
- **Códigos de Respuesta:** `200 OK`
- **Ejemplo Response (`200 OK`):**
```json
[
  {
    "id": 15,
    "productoId": 1,
    "pedidoId": 102,
    "tipo": "DESCUENTO",
    "cantidad": 5,
    "fecha": "2026-11-08T14:30:00"
  },
  {
    "id": 14,
    "productoId": 1,
    "pedidoId": null,
    "tipo": "AJUSTE",
    "cantidad": 2,
    "fecha": "2026-11-08T12:00:00"
  }
]
```

### 5.2 Consultar Productos con Stock Bajo
- **Método y Ruta:** `GET /api/inventario/stock-bajo`
- **Criterio:** Productos activos (`activo = true`) cuyas existencias son menores o iguales al umbral configurado (`existencias <= 10`, incluye agotados con `existencias = 0`).
- **Códigos de Respuesta:** `200 OK`
- **Ejemplo Response (`200 OK`):**
```json
[
  {
    "id": 2,
    "codigo": "CLO-002",
    "nombre": "Cloro Blanqueador Desinfectante 3.8L",
    "precio": 16800.00,
    "existencias": 8,
    "activo": true,
    "stockBajo": true,
    "createdAt": "2026-11-08T10:00:00"
  }
]
```

---

## 6. Contrato Interno para el Módulo de Pedidos (`Persona 3`)

La Persona 3 encargada del módulo de Pedidos interactúa con el inventario inyectando directamente el servicio Spring `InventarioService`:

```java
package com.ladorada.sistema.inventario.service;

public class InventarioService {

    public record LineaInventario(Long productoId, int cantidad) {}

    @Transactional(propagation = Propagation.MANDATORY)
    public void descontar(Long pedidoId, List<LineaInventario> lineas);

    @Transactional(propagation = Propagation.MANDATORY)
    public void restituir(Long pedidoId, List<LineaInventario> lineas);
}
```

### Reglas y Garantías de Integración:
1. **Transaccionalidad Obligatoria (`Propagation.MANDATORY`):**  
   Ambos métodos exigen que el servicio de pedidos ya haya abierto una transacción (`@Transactional`). No pueden ejecutarse aislados para garantizar atomicidad: si falla el pedido o el inventario, todo hace rollback.
2. **Prevención de Deadlocks:**  
   Las líneas se ordenan automáticamente de forma ascendente por `productoId` antes de bloquear las filas en la base de datos.
3. **Bloqueo Pesimista de Escritura:**  
   Cada producto se consulta con `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`), impidiendo condiciones de carrera entre pedidos concurrentes.
4. **Validación de Cantidades:**  
   Cualquier cantidad `<= 0` lanza `IllegalArgumentException` inmediatamente.
5. **Manejo de Stock Insuficiente:**  
   Si uno o varios productos no tienen existencias suficientes:
   - **NO se descuenta ningún producto.**
   - Se lanza `StockInsuficienteException` con la lista de `DetalleStockInsuficiente`.
   - Mensaje generado: `"Cloro 2 L: pediste 10 y hay 8 disponibles"`.
   - Si se expone vía REST en el controlador de pedidos, el `GlobalExceptionHandler` ya existente devolverá automáticamente un `HTTP 409 Conflict` con la propiedad `detallesStock`.
6. **Restitución:**  
   Suma las cantidades de vuelta al stock y registra automáticamente los movimientos de tipo `RESTITUCION`.
