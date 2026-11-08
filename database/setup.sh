#!/usr/bin/env bash
set -e

echo "======================================================="
echo "  La Dorada S.A.S. - Inicializacion de Base de Datos"
echo "======================================================="
echo ""
echo "Este script ejecutara en orden:"
echo "  1. 00_create_database.sql (Base de datos y usuario)"
echo "  2. 01_schema.sql          (Tablas, restricciones e indices)"
echo "  3. 02_seed.sql            (Datos semilla iniciales)"
echo ""
echo "Nota: Se solicitara la contrasena del usuario root de MySQL."
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "[1/3] Ejecutando 00_create_database.sql..."
if ! mysql -u root -p --default-character-set=utf8mb4 < "$SCRIPT_DIR/00_create_database.sql"; then
    echo "[ERROR] Fallo al ejecutar 00_create_database.sql. Proceso abortado."
    exit 1
fi

echo "[2/3] Ejecutando 01_schema.sql..."
if ! mysql -u root -p --default-character-set=utf8mb4 < "$SCRIPT_DIR/01_schema.sql"; then
    echo "[ERROR] Fallo al ejecutar 01_schema.sql. Proceso abortado."
    exit 1
fi

echo "[3/3] Ejecutando 02_seed.sql..."
if ! mysql -u root -p --default-character-set=utf8mb4 < "$SCRIPT_DIR/02_seed.sql"; then
    echo "[ERROR] Fallo al ejecutar 02_seed.sql. Proceso abortado."
    exit 1
fi

echo ""
echo "======================================================="
echo " Base de datos 'ladorada' configurada exitosamente!"
echo "======================================================="
