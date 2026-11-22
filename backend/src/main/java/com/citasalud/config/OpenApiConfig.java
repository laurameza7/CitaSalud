package com.citasalud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Documentación Swagger en /swagger-ui.html con el botón Authorize para pegar el token. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacion() {
        return new OpenAPI()
                .info(new Info().title("CitaSalud API")
                        .description("API REST para agendar y gestionar citas médicas")
                        .version("1.0.0"))
                .components(new Components().addSecuritySchemes("jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("jwt"));
    }
}
