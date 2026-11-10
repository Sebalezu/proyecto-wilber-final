# 02 - Diagramas de Procesos del Sistema

**Proyecto:** La Dorada S.A.S. – Sistema de Pedidos e Inventario  
**Versión:** 1.0.0  
**Fecha:** 2026-11-10  

---

## 1. Flujo Global del Ciclo de Vida de Pedidos

El siguiente diagrama modela la interacción entre los actores (**Vendedor**, **Administrador**, **Bodega**) y el **Sistema Transaccional**:

```mermaid
flowchart TD
    Inicio([Inicio: Cliente solicita cotización/compra]) --> Venta[Vendedor registra líneas de pedido en Angular]
    
    Venta --> DecisionTipoGuardado{¿Modalidad de guardado?}
    
    DecisionTipoGuardado -->|Guardar como Pendiente| PedidoPendiente[Pedido creado en estado PENDIENTE<br/>stock_descontado = false]
    DecisionTipoGuardado -->|Confirmar Pedido Directo| ValidacionFase1
    
    PedidoPendiente --> AccionPendiente{Acción sobre Pendiente}
    
    AccionPendiente -->|Modificar Líneas| ModifLineas[PUT /api/pedidos/{id}<br/>Recálculo de subtotales y total]
    ModifLineas --> PedidoPendiente
    
    AccionPendiente -->|Rechazar Pedido| Rechazar[PATCH estado = RECHAZADO<br/>Sin movimientos de stock]
    Rechazar --> FinRechazado([Fin: Pedido Rechazado - Terminal])
    
    AccionPendiente -->|Cancelar Preventivo| CancelarPrev[PATCH estado = CANCELADO<br/>Sin movimientos de stock]
    CancelarPrev --> FinCancelado([Fin: Pedido Cancelado - Terminal])
    
    AccionPendiente -->|Confirmar Pedido| ValidacionFase1
    
    subgraph TransaccionConfirmacion ["Transacción ACID de Confirmación"]
        ValidacionFase1[Fase 1: Bloqueo Pesimista de Productos<br/>SELECT FOR UPDATE ordenados por ID] --> CheckStock{¿Todas las líneas<br/>tienen existencias suficientes?}
        
        CheckStock -->|No| RollbackConfirm[Rollback de transacción<br/>Lanza StockInsuficienteException 409]
        CheckStock -->|Sí| Fase2[Fase 2: Aplicar descuentos en producto<br/>Registrar movimientos DESCUENTO en Kardex]
        Fase2 --> SetConfirmado[Cambio a estado CONFIRMADO<br/>stock_descontado = true]
    end
    
    RollbackConfirm --> ErrorVisual[Frontend muestra alerta roja #FDE5E3<br/>con detalle: 'Producto X: pediste N y hay M disponibles']
    ErrorVisual --> PedidoPendiente
    
    SetConfirmado --> NotifBodega[Bodega visualiza pedido confirmado en cola]
    
    NotifBodega --> AccionConfirmado{Acción de Bodega / Admin}
    
    AccionConfirmado -->|Despachar Mercancía| Despacho[Bodega embala y despacha<br/>PATCH estado = DESPACHADO]
    Despacho --> FinDespachado([Fin: Pedido Despachado - Éxito Terminal])
    
    AccionConfirmado -->|Cancelar Pedido Confirmado| CancelacionRestitucion[Confirmación de usuario:<br/>¿Restituir inventario a bodega?]
    
    CancelacionRestitucion --> TransaccionRestitucion[Bloqueo pesimista de productos<br/>Reintegro de existencias a producto<br/>Registrar movimientos RESTITUCION<br/>PATCH estado = CANCELADO<br/>stock_descontado = false]
    
    TransaccionRestitucion --> FinCancelado
```

---

## 2. Diagrama de Secuencia: Confirmación con Validación en 2 Fases y Bloqueo Pesimista

Este diagrama detalla cómo se previene la concurrencia y sobreventa cuando se confirma un pedido:

```mermaid
sequenceDiagram
    autonumber
    actor Usuario as Vendedor / Admin
    participant UI as Frontend (Angular)
    participant Ctrl as PedidoController
    participant PedSvc as PedidoService
    participant InvSvc as InventarioService
    participant Repo as ProductoRepository (DB)
    participant MovRepo as MovimientoRepo (DB)

    Usuario->>UI: Clic en "Confirmar Pedido"
    UI->>Ctrl: PATCH /api/pedidos/100/estado { estado: "CONFIRMADO" }
    Ctrl->>PedSvc: cambiarEstado(100, CONFIRMADO)
    
    critical Transacción @Transactional
        PedSvc->>PedSvc: Validar transición PENDIENTE -> CONFIRMADO
        PedSvc->>InvSvc: descontar(pedidoId=100, lineas)
        
        rect rgb(240, 248, 255)
            note over InvSvc, Repo: FASE 1: Agrupar por productoId ascendente y Validar Todo
            InvSvc->>Repo: findByIdConBloqueo(prodId=1) [PESSIMISTIC_WRITE]
            Repo-->>InvSvc: Producto 1 (Existencias: 8)
            InvSvc->>Repo: findByIdConBloqueo(prodId=2) [PESSIMISTIC_WRITE]
            Repo-->>InvSvc: Producto 2 (Existencias: 15)
            
            alt Existencias Insuficientes en algún producto
                InvSvc-->>PedSvc: throw StockInsuficienteException (detalles)
                PedSvc-->>Ctrl: Rollback total de transacción
                Ctrl-->>UI: HTTP 409 Conflict + ErrorRespuesta
                UI-->>Usuario: Muestra banner rojo #FDE5E3 con productos faltantes
            else Existencias Suficientes en Todos
                note over InvSvc, MovRepo: FASE 2: Aplicar Descuentos y Movimientos
                InvSvc->>Repo: save(Producto 1 con existencias - solicitadas)
                InvSvc->>MovRepo: save(MovimientoInventario: DESCUENTO)
                InvSvc->>Repo: save(Producto 2 con existencias - solicitadas)
                InvSvc->>MovRepo: save(MovimientoInventario: DESCUENTO)
                InvSvc-->>PedSvc: Descuento aplicado satisfactoriamente
            end
        end
        
        PedSvc->>PedSvc: pedido.setEstado(CONFIRMADO), stock_descontado = true
        PedSvc->>PedSvc: registrarHistorial(PENDIENTE -> CONFIRMADO)
    end
    
    PedSvc-->>Ctrl: PedidoDetalleResponse actualizado
    Ctrl-->>UI: HTTP 200 OK
    UI-->>Usuario: Stepper avanza a CONFIRMADO, badge azul, botón Despachar activo
```

---

## 3. Diagrama de Secuencia: Cancelación de Pedido Confirmado con Restitución

```mermaid
sequenceDiagram
    autonumber
    actor Bodega as Usuario Bodega / Admin
    participant UI as Frontend (Angular)
    participant Ctrl as PedidoController
    participant PedSvc as PedidoService
    participant InvSvc as InventarioService
    participant ProdRepo as ProductoRepository
    participant MovRepo as MovimientoRepo

    Bodega->>UI: Clic en "Cancelar Pedido (Restituye Stock)"
    UI->>Bodega: Diálogo modal: "¿Desea cancelar el pedido?<br/>El inventario reservado será restituido a bodega automáticamente."
    Bodega->>UI: Confirma Cancelación
    
    UI->>Ctrl: PATCH /api/pedidos/200/estado { estado: "CANCELADO" }
    Ctrl->>PedSvc: cambiarEstado(200, CANCELADO)
    
    critical Transacción @Transactional
        PedSvc->>PedSvc: Verifica stockDescontado == true
        PedSvc->>InvSvc: restituir(pedidoId=200, lineas)
        
        loop Por cada producto consolidado
            InvSvc->>ProdRepo: findByIdConBloqueo(productoId) [PESSIMISTIC_WRITE]
            InvSvc->>ProdRepo: save(existencias + cantidad)
            InvSvc->>MovRepo: save(MovimientoInventario: RESTITUCION)
        end
        
        PedSvc->>PedSvc: pedido.setEstado(CANCELADO), stockDescontado = false
        PedSvc->>PedSvc: registrarHistorial(CONFIRMADO -> CANCELADO)
    end
    
    PedSvc-->>Ctrl: PedidoDetalleResponse actualizado
    Ctrl-->>UI: HTTP 200 OK
    UI-->>Bodega: Stepper cambia a CANCELADO, banner rojo de advertencia, auditoría actualizada
```

---

## 4. Diagrama de Proceso: Ingreso de Mercancía / Ajuste Físico (+ Stock)

```mermaid
sequenceDiagram
    autonumber
    actor Bodega as Usuario Bodega / Admin
    participant UI as Frontend (/catalogo)
    participant Ctrl as InventarioController
    participant Svc as InventarioService
    participant Repo as ProductoRepository
    participant MovRepo as MovimientoRepo

    Bodega->>UI: En la fila del producto, clic en "+ Stock"
    UI->>Bodega: Despliega modal con código, nombre y existencias actuales
    Bodega->>UI: Ingresa cantidad a sumar (ej. 20) y motivo
    UI->>Bodega: Muestra proyección: "Nuevas existencias: X + 20 unidades"
    Bodega->>UI: Clic en "Confirmar Ingreso"
    
    UI->>Ctrl: POST /api/inventario/ajuste { productoId, cantidad: 20, motivo }
    Ctrl->>Svc: ajustarStock(request)
    
    critical Transacción @Transactional
        Svc->>Repo: findByIdConBloqueo(productoId)
        Svc->>Repo: save(existencias + 20)
        Svc->>MovRepo: save(MovimientoInventario: AJUSTE, cantidad=20)
    end
    
    Svc-->>Ctrl: ProductoResponse actualizado
    Ctrl-->>UI: HTTP 200 OK
    UI->>UI: Actualiza fila en tabla reactivamente
    UI-->>Bodega: Muestra notificación verde: "Stock actualizado con éxito"
```

---

## 5. Diagrama de Proceso: Autenticación y Autorización por Roles

```mermaid
flowchart TD
    Peticion[Usuario ingresa credenciales en /login] --> AuthAPI[POST /api/auth/login]
    
    AuthAPI --> ValidarDB{¿Credenciales válidas<br/>y usuario activo?}
    
    ValidarDB -->|No| Err401[Retorna HTTP 401 Unauthorized<br/>Credenciales inválidas o usuario inactivo]
    Err401 --> AlertaLogin[Frontend muestra alerta en rojo]
    
    ValidarDB -->|Sí| Ok200[Retorna HTTP 200 OK<br/>id, nombre, email, rol]
    Ok200 --> GuardarLocal[Frontend almacena en localStorage<br/>Emite en AuthService.usuarioActual$]
    
    GuardarLocal --> Routing{Navegación en Frontend}
    
    Routing -->|/inicio| Todos[Accesible para todos los roles]
    
    Routing -->|/nuevo-pedido| GuardRole1{AuthGuard + RoleGuard<br/>roles: ADMIN, VENDEDOR}
    GuardRole1 -->|Permitido| VistaNuevoPed[Carga formulario de pedido]
    GuardRole1 -->|Denegado (Bodega)| RedirInicio1[Redirige a /inicio]
    
    Routing -->|/catalogo| VistaCatalogo[Consulta de catálogo: Todos]
    VistaCatalogo --> BotonesCatalogo{Rol del Usuario}
    BotonesCatalogo -->|ADMINISTRADOR| BtnsAdmin[Visible: Crear, Editar, Activar/Desactivar, +Stock]
    BotonesCatalogo -->|BODEGA| BtnsBodega[Visible: +Stock para ingreso físico]
    BotonesCatalogo -->|VENDEDOR| BtnsVendedor[Modo Solo Lectura de catálogo]
    
    Routing -->|/pedidos/:id| VistaDetalle[Detalle de orden: Todos]
    VistaDetalle --> AccionesPedido{Estado del Pedido y Rol}
    AccionesPedido -->|PENDIENTE + (ADMIN o VENDEDOR)| BtnConfirmar[Habilitado: Confirmar, Modificar, Rechazar, Cancelar]
    AccionesPedido -->|CONFIRMADO + (ADMIN o BODEGA)| BtnDespacho[Habilitado: Despachar, Cancelar con Restitución]
    AccionesPedido -->|CONFIRMADO + VENDEDOR| BloqueoVentas[Sin acciones disponibles de transición]
```
