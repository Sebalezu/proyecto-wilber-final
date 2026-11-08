# Backend – La Dorada S.A.S.

API REST desarrollada con **Java 17** y **Spring Boot 3**, gestionada con **Maven**.

## Arquitectura de Paquetes
```
com.ladorada.sistema/
├── SistemaApplication.java
├── catalogo/       (controller, service, repository, model, dto)
├── inventario/     (controller, service, repository, model, dto)
├── pedidos/        (controller, service, repository, model, dto)
└── common/         (config, exception, health)
```

## Requisitos Previos
- **Java 17** (o compatible JDK 17+).
- **Maven 3.8+** (o usar el Maven Wrapper `./mvnw` incluido).
- Instancia local de **MySQL 8** en ejecución con la base de datos `ladorada` previamente inicializada mediante `database/setup.bat` o `database/setup.sh`.

## Variables de Entorno de Configuración
La configuración en `src/main/resources/application.yml` permite sobreescribir los siguientes parámetros:

| Variable | Descripción | Valor por Defecto |
| :--- | :--- | :--- |
| `DB_HOST` | Host del servidor MySQL | `localhost` |
| `DB_PORT` | Puerto de MySQL | `3306` |
| `DB_NAME` | Nombre de la base de datos | `ladorada` |
| `DB_USER` | Usuario de desarrollo | `ladorada_user` |
| `DB_PASSWORD` | Contraseña del usuario | `ladorada123` |
| `CORS_ALLOWED_ORIGINS` | Orígenes CORS habilitados | `http://localhost:4200` |

## Comandos de Ejecución

### Compilar el proyecto
```bash
./mvnw clean compile
# o en Windows:
mvnw.cmd clean compile
```

### Ejecutar en modo desarrollo
```bash
./mvnw spring-boot:run
# o en Windows:
mvnw.cmd spring-boot:run
```

### Probar endpoint de salud
Una vez levantado el servicio:
```bash
curl http://localhost:8080/api/health
# Respuesta: {"status":"ok"}
```

### Documentación Swagger / OpenAPI
Con el backend en ejecución, accede a la interfaz gráfica interactiva en:
- `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI JSON: `http://localhost:8080/api-docs`
