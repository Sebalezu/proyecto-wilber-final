# Base de Datos – La Dorada S.A.S.

Directorio de scripts SQL y utilidades para la gestión de la base de datos MySQL 8 local del sistema.

## Requisitos
- MySQL 8.x instalado y en ejecución en `localhost:3306`.
- Cliente de línea de comandos `mysql` disponible en el `PATH` (o MySQL Workbench).

---

## Estructura de Scripts
1. **`00_create_database.sql`**: Crea la base de datos `ladorada` (con codificación `utf8mb4` y cotejamiento `utf8mb4_unicode_ci`) y el usuario `ladorada_user` con clave `ladorada123` otorgando todos los privilegios sobre dicha base.
2. **`01_schema.sql`**: Define el esquema relacional DDL (tablas `producto`, `cliente`, `pedido`, `pedido_linea`, `pedido_historial_estado` y `movimiento_inventario`), claves primarias, claves foráneas, índices de optimización y restricciones `CHECK`.
3. **`02_seed.sql`**: Inserta datos iniciales de prueba (5 productos de aseo, 4 clientes y 3 pedidos con diferentes estados).

---

## Ejecución desde la Terminal (Recomendado)

### En Windows
Ejecuta el script por lotes desde la raíz del proyecto o dentro de `database/`:
```cmd
cd database
setup.bat
```
El script te solicitará la contraseña de `root` de MySQL para cada paso e informará si hubo algún error.

### En Linux / macOS
Otorga permisos de ejecución y ejecuta:
```bash
chmod +x database/setup.sh
cd database
./setup.sh
```

---

## Ejecución desde MySQL Workbench
1. Abre MySQL Workbench y conéctate como `root`.
2. Menú **File** > **Open SQL Script...** y abre en orden:
   - `00_create_database.sql` -> Clic en el icono del rayo (Execute).
   - `01_schema.sql` -> Clic en el icono del rayo (Execute).
   - `02_seed.sql` -> Clic en el icono del rayo (Execute).
3. En el panel **Schemas**, haz clic derecho y selecciona **Refresh All** para verificar las 6 tablas creadas.

---

## Cómo reiniciar la base de datos desde cero
Si necesitas limpiar y recrear completamente la base de datos:

### Opción 1: Ejecutar sentencia DROP en MySQL
Ingresa a MySQL como `root`:
```sql
DROP DATABASE IF EXISTS ladorada;
```
Y luego vuelve a ejecutar `setup.bat` o `setup.sh`.

### Opción 2: Reejecución directa de scripts
Ejecutar nuevamente `setup.bat` o `setup.sh` volverá a cargar las tablas y la información requerida.
