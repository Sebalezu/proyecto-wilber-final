package com.ladorada.sistema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DatabaseConnectionIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Verificar conexión a MySQL local y validación de entidades JPA")
    void testConexionYValidacionEsquema() throws Exception {
        assertNotNull(dataSource, "El DataSource debe estar configurado");

        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "La conexión a la base de datos debe ser válida");
            assertFalse(connection.isClosed(), "La conexión a MySQL debe estar activa");

            DatabaseMetaData metaData = connection.getMetaData();
            String productName = metaData.getDatabaseProductName();
            assertNotNull(productName);
            assertTrue(productName.toLowerCase().contains("mysql"),
                    "El motor de base de datos debe ser MySQL (obtenido: " + productName + ")");
        }
    }
}

