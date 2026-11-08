# La Dorada S.A.S. – Sistema de Pedidos e Inventario

Monorepo del sistema de gestión de pedidos e inventario para la distribuidora de productos de aseo y desinfección **La Dorada S.A.S.**

---

## 🛠️ Stack Tecnológico

- **Base de Datos:** MySQL 8.x (instalación local, sin Docker).
- **Backend:** Java 17 + Spring Boot 3 (Maven), API REST con documentación Swagger/OpenAPI.
- **Frontend:** Angular 17+ (Standalone Components, SCSS, Routing, diseño mobile-first).
- **Control de Versiones:** Git (estrategia de ramas `main` y `develop`).

---

## 👥 Matriz de Responsabilidades del Equipo

| Rol | Miembro | Responsabilidades Principales |
| :--- | :--- | :--- |
| **Arquitectura & Datos** | **Persona 1** | Modelado y scripts de Base de Datos MySQL, integración y consumo de servicios en Frontend, pruebas integrales (E2E/Unitarias). |
| **Backend Catálogo & Stock** | **Persona 2** | Desarrollo de API REST de Catálogo de Productos y Movimientos de Inventario (Kardex, ajustes, existencias). |
| **Backend Pedidos** | **Persona 3** | Desarrollo de API REST del flujo y ciclo de vida de Pedidos (transiciones de estado, cálculo de totales, descuento/restitución de stock). |
| *Diseño UI/UX* | *Externo* | Diseño visual y maquetación inicial ya provistos e integrados en el frontend base. |

---

## 📁 Estructura del Monorepo

```
la-dorada-sas/
├── README.md                           # Documentación general del monorepo
├── .gitignore                          # Reglas de exclusión de Git
├── database/                           # Scripts SQL y automatización de base de datos
│   ├── 00_create_database.sql          # Creación de BD 'ladorada' y usuario de desarrollo
│   ├── 01_schema.sql                   # Tablas, claves, índices y restricciones CHECK
│   ├── 02_seed.sql                     # Datos semilla (productos de aseo, clientes, pedidos)
│   ├── setup.bat                       # Script de instalación para Windows
│   ├── setup.sh                        # Script de instalación para Linux / macOS
│   └── README.md                       # Guía de base de datos y restauración
├── backend/                            # API REST Spring Boot 3 + Java 17
│   ├── pom.xml                         # Descriptores y dependencias Maven
│   ├── mvnw / mvnw.cmd                 # Maven Wrapper (ejecución sin Maven instalado)
│   ├── README.md                       # Guía técnica del backend y endpoints
│   └── src/main/
│       ├── java/com/ladorada/sistema/
│       │   ├── SistemaApplication.java # Clase principal de arranque
│       │   ├── catalogo/               # (controller, service, repository, model, dto)
│       │   ├── inventario/             # (controller, service, repository, model, dto)
│       │   ├── pedidos/                # (controller, service, repository, model, dto)
│       │   └── common/                 # (config, exception, health check)
│       └── resources/
│           └── application.yml         # Configuración del datasource y CORS
├── frontend/                           # Aplicación Web Angular (la-dorada-web)
│   ├── angular.json                    # Configuración de compilación Angular
│   ├── package.json                    # Dependencias npm
│   ├── README.md                       # Documentación técnica del frontend
│   └── src/
│       ├── styles.scss                 # Estilos globales y clases utilitarias
│       ├── styles/
│       │   └── _variables.scss         # Paleta corporativa (frescura, higiene y confianza)
│       ├── environments/               # Variables de entorno (URL del backend)
│       └── app/
│           ├── core/                   # (api.service.ts, interceptors)
│           ├── shared/                 # (componentes reutilizables, modelos)
│           └── features/               # Módulos y vistas principales:
│               ├── inicio/             # Panel de bienvenida (/inicio)
│               ├── catalogo/           # Catálogo de productos (/catalogo)
│               ├── nuevo-pedido/       # Registro de pedidos (/nuevo-pedido)
│               ├── pedidos/            # Listado histórico (/pedidos)
│               └── pedido-detalle/     # Detalle y estados (/pedidos/:id)
└── docs/                               # Documentación de análisis y arquitectura
    ├── 01-analisis-contexto.md
    ├── 02-diagramas-procesos.md
    ├── 03-requerimientos.md
    ├── 04-arquitectura.md
    ├── 05-modelo-datos.md
    ├── 06-contrato-api.md
    ├── 07-pruebas.md
    └── mockups/                        # Diseños y bocetos de interfaz
```

---

## 🚀 Guía de Puesta en Marcha (Paso a Paso)

### 1. Prerrequisitos de Software
- **MySQL 8.x** instalado y corriendo en el puerto local por defecto (`3306`).
- **Java 17 JDK** (o superior compatible) instalado.
- **Node.js** (versión LTS recomendada) y **npm**.

---

### 2. Paso 1: Configurar la Base de Datos
Ejecuta el asistente automatizado para crear la base de datos `ladorada`, el usuario de desarrollo `ladorada_user` con clave `ladorada123`, el esquema relacional con restricciones y los datos iniciales de prueba:

- **En Windows:**
  ```cmd
  cd database
  setup.bat
  ```

- **En Linux / macOS:**
  ```bash
  chmod +x database/setup.sh
  cd database
  ./setup.sh
  ```

*(Te solicitará la contraseña de tu usuario `root` de MySQL para ejecutar los scripts en orden).*

---

### 3. Paso 2: Ejecutar el Backend
Desde la raíz del proyecto, accede a la carpeta `backend/` e inicia el servidor con Spring Boot:

- **En Windows (usando Maven Wrapper):**
  ```cmd
  cd backend
  mvnw.cmd spring-boot:run
  ```

- **En Linux / macOS:**
  ```bash
  cd backend
  ./mvnw spring-boot:run
  ```

- **Si tienes Maven instalado globalmente:**
  ```bash
  cd backend
  mvn spring-boot:run
  ```

**Comprobar funcionamiento:**
- Endpoint de salud: [http://localhost:8080/api/health](http://localhost:8080/api/health) &rarr; `{"status":"ok"}`
- Documentación Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

### 4. Paso 3: Ejecutar el Frontend
Desde una nueva terminal, navega a la carpeta `frontend/`:

```bash
cd frontend
npm install
ng serve
# o si no tienes Angular CLI global:
npm start
```

Abre tu navegador en: [http://localhost:4200](http://localhost:4200).

---

## 🎨 Paleta de Identidad Visual (La Dorada S.A.S.)
Inspirada en frescura, higiene y confiabilidad:
- **Primario:** `#0B6E78` (Azul petróleo / higiénico)
- **Fondo:** `#F3FAFB` (Blanco gélido)
- **Aqua Suave:** `#DDF3F4` (Resaltados y selección activa)
- **Superficie:** `#FFFFFF` (Tarjetas y contenedores)
- **Texto Principal:** `#12333A` | **Secundario:** `#4F6B72` | **Borde:** `#D6E8EB`
- **Etiquetas de Estado:**
  - *Pendiente:* Fondo `#FFF3D6` | Texto `#7A5200`
  - *Confirmado:* Fondo `#E1EEFB` | Texto `#14508A`
  - *Despachado:* Fondo `#DDF5E8` | Texto `#17673F`
  - *Rechazado/Cancelado:* Fondo `#FDE5E3` | Texto `#8F2A21`
