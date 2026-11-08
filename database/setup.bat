@echo off
setlocal enabledelayedexpansion

echo =======================================================
echo   La Dorada S.A.S. - Inicializacion de Base de Datos
echo =======================================================
echo.
echo Este script ejecutara en orden:
echo   1. 00_create_database.sql (Base de datos y usuario)
echo   2. 01_schema.sql          (Tablas, restricciones e indices)
echo   3. 02_seed.sql            (Datos semilla iniciales)
echo.
echo Nota: Se requerira la contrasena del usuario root de MySQL.
echo.

set SCRIPT_DIR=%~dp0

echo [1/3] Ejecutando 00_create_database.sql...
mysql -u root -p --default-character-set=utf8mb4 < "%SCRIPT_DIR%00_create_database.sql"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Fallo al ejecutar 00_create_database.sql. Proceso abortado.
    exit /b %ERRORLEVEL%
)

echo [2/3] Ejecutando 01_schema.sql...
mysql -u root -p --default-character-set=utf8mb4 < "%SCRIPT_DIR%01_schema.sql"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Fallo al ejecutar 01_schema.sql. Proceso abortado.
    exit /b %ERRORLEVEL%
)

echo [3/3] Ejecutando 02_seed.sql...
mysql -u root -p --default-character-set=utf8mb4 < "%SCRIPT_DIR%02_seed.sql"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Fallo al ejecutar 02_seed.sql. Proceso abortado.
    exit /b %ERRORLEVEL%
)

echo.
echo =======================================================
echo  Base de datos 'ladorada' configurada exitosamente!
echo =======================================================
