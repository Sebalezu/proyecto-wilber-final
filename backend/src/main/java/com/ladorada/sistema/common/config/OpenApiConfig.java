package com.ladorada.sistema.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("La Dorada S.A.S. – API de Pedidos e Inventario")
                        .version("1.0.0")
                        .description("API REST para la gestión de productos, clientes, pedidos e inventario de productos de aseo y desinfección.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo La Dorada S.A.S.")
                                .email("soporte@ladorada.com")));
    }
}
