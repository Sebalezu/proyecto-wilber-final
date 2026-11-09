# 01 - Análisis de Contexto del Negocio

**Empresa:** La Dorada S.A.S.  
**Sector:** Distribución de productos de aseo, limpieza y desinfección institucional y doméstico  
**Proyecto:** Sistema de Gestión de Pedidos e Inventario  
**Versión:** 1.0.0  
**Fecha:** 2026-11-09  

---

## 1. Introducción y Resumen Ejecutivo

**La Dorada S.A.S.** es una empresa comercializadora y distribuidora de insumos de aseo y desinfección (detergentes industriales, cloros, jabones antibacteriales, desinfectantes multiusos, bolsas para residuos y accesorios de limpieza), orientada tanto al sector institucional (restaurantes, colegios, minimarkets, pizzerías y hoteles) como a clientes minoristas en el territorio regional.

Con el crecimiento de su base de clientes y la aceleración de su volumen de ventas, la empresa se enfrentó a la necesidad crítica de digitalizar y centralizar sus operaciones operativas para erradicar las fallas de coordinación entre la fuerza de ventas y el almacén general de bodega.

---

## 2. Problemática Identificada

Antes de la implementación del sistema, La Dorada S.A.S. operaba con registros dispersos, planillas físicas y comunicaciones informales (mensajería instantánea), generando las siguientes fricciones:

1. **Sobreventa de Stock (Ruptura de Inventario):**  
   Los vendedores registraban pedidos y prometían entregas sobre productos cuyo stock real ya se encontraba agotado o comprometido en otras órdenes, ocasionando incumplimientos y deterioro en la relación comercial.
2. **Falta de Trazabilidad en los Estados del Pedido:**  
   No existía visibilidad en tiempo real para saber si una orden de compra estaba en revisión, confirmada, empacada en bodega o despachada.
3. **Pérdida de Auditoría en Fluctuaciones de Existencias (Kardex Incompleto):**  
   Los ajustes físicos de bodega por llegadas de camión proveedor, roturas o devoluciones de clientes no se registraban con marcas de tiempo ni justificaciones auditables.
4. **Devoluciones y Cancelaciones Traumáticas:**  
   Cuando un pedido confirmado era cancelado por el cliente, el inventario reservado no se reintegraba de forma inmediata al sistema, provocando pérdidas de oportunidad de venta para otros clientes.

---

## 3. Objetivos del Sistema

### 3.1 Objetivo General
Desarrollar e implementar un sistema web integral, responsivo y transaccional que centralice la gestión del catálogo de productos, el inventario físico (Kardex) y el ciclo de vida completo de los pedidos de La Dorada S.A.S., garantizando consistencia atómica en las existencias y control de acceso basado en roles.

### 3.2 Objetivos Específicos
- **Garantizar Integridad Transaccional:** Implementar validación previa en dos fases y bloqueo pesimista en base de datos para impedir que dos vendedores descuenten el mismo stock simultáneamente.
- **Trazabilidad de Auditoría Completa:** Registrar cronológicamente cada cambio de estado de los pedidos y cada movimiento de inventario (`DESCUENTO`, `RESTITUCION`, `AJUSTE`).
- **Control de Acceso Basado en Roles:** Proveer perfiles diferenciados (`ADMINISTRADOR`, `VENDEDOR`, `BODEGA`) protegiendo las operaciones críticas del negocio.
- **Monitoreo Proactivo de Existencias:** Emitir alertas tempranas de stock bajo ($\le 10$ unidades) y estado crítico ($\le 5$ unidades) para gestionar compras y reposiciones a tiempo.
- **Experiencia de Usuario Moderna:** Proveer una interfaz limpia, mobile-first, con soporte para modo oscuro automático y formateo de precios en pesos colombianos ($ COP).

---

## 4. Alcance del Sistema

### 4.1 En Alcance (In Scope)
- Autenticación de usuarios y asignación de roles.
- Catálogo maestro de productos con generación de código consecutivo automático (`ASE-XXX`).
- Gestión de clientes institucionales (creación y consulta rápida).
- Flujo completo de pedidos: Borrador (`PENDIENTE`), Confirmación con descuento atómico (`CONFIRMADO`), Despacho logístico (`DESPACHADO`), Cancelación con restitución automática (`CANCELADO`) y Rechazo (`RECHAZADO`).
- Módulo de almacén: Ingreso de mercancía / ajuste físico (`+ Stock`) y monitoreo de existencias.
- Interfaz gráfica SPA en Angular y API REST en Spring Boot 3 respaldada por MySQL 8.

### 4.2 Fuera de Alcance (Out of Scope)
- Facturación electrónica DIAN (la orden de pedido sirve como insumo comercial interno).
- Pasarelas de pago con tarjeta en línea (las órdenes se liquidan contra entrega o por crédito comercial).
- Ruteo por GPS de vehículos de entrega.

---

## 5. Stakeholders y Perfiles de Usuario

| Perfil | Área | Funciones Principales en el Sistema |
| :--- | :--- | :--- |
| **Gerencia General (Administrador)** | Dirección | Supervisión de métricas, configuración de productos y precios, auditoría de Kardex y resolución de excepciones operativas. |
| **Fuerza de Ventas (Vendedor)** | Comercial | Creación de cotizaciones y pedidos, seguimiento de pedidos de sus clientes y verificación de stock disponible en tiempo real. |
| **Jefe de Bodega / Logística (Bodega)** | Operaciones | Recepción de insumos de proveedores (`Ajuste de Stock`), control de existencias físicas, empaque y despacho de órdenes confirmadas, y cancelación de envíos con devolución de mercancía. |
| **Clientes (Restaurantes, Minimarkets)** | Externo | Receptores finales de los pedidos y facturas de venta. |
