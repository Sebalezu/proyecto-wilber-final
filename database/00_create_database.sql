-- =====================================================================
-- Creación de Base de Datos y Usuario de Desarrollo
-- Proyecto: La Dorada S.A.S. - Sistema de pedidos e inventario
-- =====================================================================

CREATE DATABASE IF NOT EXISTS ladorada
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Crear usuario para desarrollo local
CREATE USER IF NOT EXISTS 'ladorada_user'@'%' IDENTIFIED BY 'ladorada123';
CREATE USER IF NOT EXISTS 'ladorada_user'@'localhost' IDENTIFIED BY 'ladorada123';

-- Asignar privilegios completos sobre la base de datos ladorada
GRANT ALL PRIVILEGES ON ladorada.* TO 'ladorada_user'@'%';
GRANT ALL PRIVILEGES ON ladorada.* TO 'ladorada_user'@'localhost';

FLUSH PRIVILEGES;
