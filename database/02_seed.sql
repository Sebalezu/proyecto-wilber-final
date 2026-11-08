-- =====================================================================
-- Datos Semilla (Seed) de Prueba
-- Proyecto: La Dorada S.A.S. - Sistema de pedidos e inventario
-- =====================================================================

USE ladorada;

-- 1. Insertar 5 productos de aseo y limpieza
INSERT INTO producto (id, codigo, nombre, precio, existencias, activo, created_at) VALUES
(1, 'DET-001', 'Detergente Líquido Industrial 5L', 38500.00, 120, TRUE, NOW()),
(2, 'CLO-002', 'Cloro Blanqueador Desinfectante 3.8L', 16800.00, 85, TRUE, NOW()),
(3, 'DES-003', 'Desinfectante Multiusos Aroma Lavanda 4L', 22900.00, 95, TRUE, NOW()),
(4, 'JAB-004', 'Jabón Líquido Antibacterial para Manos 1L', 14500.00, 150, TRUE, NOW()),
(5, 'BOL-005', 'Bolsas para Basura Negras Extra Grandes (Paquete x 100)', 28000.00, 60, TRUE, NOW());

-- 2. Insertar 4 clientes (tiendas y restaurantes)
INSERT INTO cliente (id, nombre, telefono) VALUES
(1, 'Restaurante El Sabor Costeño', '+57 310 555 1234'),
(2, 'Minimarket La Economía', '+57 315 888 4321'),
(3, 'Pizzería Trattoria Bella', '+57 301 222 9876'),
(4, 'Tienda de Abarrotes San Jorge', '+57 318 777 6543');

-- 3. Insertar 3 pedidos en estados distintos

-- Pedido 1: Estado PENDIENTE (no ha descontado stock)
INSERT INTO pedido (id, cliente_id, estado, stock_descontado, total, created_at) VALUES
(1, 1, 'PENDIENTE', FALSE, 93800.00, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO pedido_linea (id, pedido_id, producto_id, cantidad, precio_unitario) VALUES
(1, 1, 1, 2, 38500.00), -- 2 Detergentes = 77000.00
(2, 1, 2, 1, 16800.00); -- 1 Cloro = 16800.00 (Total: 93800.00)

INSERT INTO pedido_historial_estado (id, pedido_id, estado_anterior, estado_nuevo, fecha) VALUES
(1, 1, NULL, 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- Pedido 2: Estado CONFIRMADO (stock descontado)
INSERT INTO pedido (id, cliente_id, estado, stock_descontado, total, created_at) VALUES
(2, 2, 'CONFIRMADO', TRUE, 124700.00, DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO pedido_linea (id, pedido_id, producto_id, cantidad, precio_unitario) VALUES
(3, 2, 3, 3, 22900.00), -- 3 Desinfectantes = 68700.00
(4, 2, 5, 2, 28000.00); -- 2 Bolsas de basura = 56000.00 (Total: 124700.00)

INSERT INTO pedido_historial_estado (id, pedido_id, estado_anterior, estado_nuevo, fecha) VALUES
(2, 2, NULL, 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 2, 'PENDIENTE', 'CONFIRMADO', DATE_SUB(NOW(), INTERVAL 20 HOUR));

INSERT INTO movimiento_inventario (id, producto_id, pedido_id, tipo, cantidad, fecha) VALUES
(1, 3, 2, 'DESCUENTO', 3, DATE_SUB(NOW(), INTERVAL 20 HOUR)),
(2, 5, 2, 'DESCUENTO', 2, DATE_SUB(NOW(), INTERVAL 20 HOUR));

-- Pedido 3: Estado DESPACHADO (stock descontado y entregado al cliente)
INSERT INTO pedido (id, cliente_id, estado, stock_descontado, total, created_at) VALUES
(3, 3, 'DESPACHADO', TRUE, 58000.00, DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO pedido_linea (id, pedido_id, producto_id, cantidad, precio_unitario) VALUES
(5, 3, 4, 4, 14500.00); -- 4 Jabones antibacteriales = 58000.00

INSERT INTO pedido_historial_estado (id, pedido_id, estado_anterior, estado_nuevo, fecha) VALUES
(4, 3, NULL, 'PENDIENTE', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 3, 'PENDIENTE', 'CONFIRMADO', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 3, 'CONFIRMADO', 'DESPACHADO', DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO movimiento_inventario (id, producto_id, pedido_id, tipo, cantidad, fecha) VALUES
(3, 4, 3, 'DESCUENTO', 4, DATE_SUB(NOW(), INTERVAL 2 DAY));
