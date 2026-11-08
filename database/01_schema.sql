-- =====================================================================
-- Esquema de Tablas, Llaves, Índices y Restricciones CHECK
-- Proyecto: La Dorada S.A.S. - Sistema de pedidos e inventario
-- =====================================================================

USE ladorada;

-- 1. Tabla: producto
CREATE TABLE IF NOT EXISTS producto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    precio DECIMAL(12, 2) NOT NULL,
    existencias INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_producto_codigo UNIQUE (codigo),
    CONSTRAINT chk_producto_precio CHECK (precio > 0),
    CONSTRAINT chk_producto_existencias CHECK (existencias >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_producto_codigo ON producto(codigo);
CREATE INDEX idx_producto_activo ON producto(activo);

-- 2. Tabla: cliente
CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cliente_nombre ON cliente(nombre);

-- 3. Tabla: pedido
CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    estado ENUM('PENDIENTE', 'CONFIRMADO', 'DESPACHADO', 'RECHAZADO', 'CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    stock_descontado BOOLEAN NOT NULL DEFAULT FALSE,
    total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pedido_total CHECK (total >= 0),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pedido_cliente_id ON pedido(cliente_id);
CREATE INDEX idx_pedido_estado ON pedido(estado);
CREATE INDEX idx_pedido_created_at ON pedido(created_at);

-- 4. Tabla: pedido_linea
CREATE TABLE IF NOT EXISTS pedido_linea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    CONSTRAINT chk_linea_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_linea_precio CHECK (precio_unitario >= 0),
    CONSTRAINT fk_linea_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_linea_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_linea_pedido_id ON pedido_linea(pedido_id);
CREATE INDEX idx_linea_producto_id ON pedido_linea(producto_id);

-- 5. Tabla: pedido_historial_estado
CREATE TABLE IF NOT EXISTS pedido_historial_estado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    estado_anterior ENUM('PENDIENTE', 'CONFIRMADO', 'DESPACHADO', 'RECHAZADO', 'CANCELADO') NULL,
    estado_nuevo ENUM('PENDIENTE', 'CONFIRMADO', 'DESPACHADO', 'RECHAZADO', 'CANCELADO') NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_historial_pedido_id ON pedido_historial_estado(pedido_id);
CREATE INDEX idx_historial_fecha ON pedido_historial_estado(fecha);

-- 6. Tabla: movimiento_inventario
CREATE TABLE IF NOT EXISTS movimiento_inventario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    pedido_id BIGINT NULL,
    tipo ENUM('DESCUENTO', 'RESTITUCION', 'AJUSTE') NOT NULL,
    cantidad INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_movimiento_producto FOREIGN KEY (producto_id)
        REFERENCES producto (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_movimiento_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_movimiento_producto_id ON movimiento_inventario(producto_id);
CREATE INDEX idx_movimiento_pedido_id ON movimiento_inventario(pedido_id);
CREATE INDEX idx_movimiento_tipo ON movimiento_inventario(tipo);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario(fecha);
