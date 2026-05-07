package com.bankofabyssinia.letter_serial_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springTemplateOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Letter Serial API")
                .description("Reusable API template for Bank of Abyssinia projects")
                .version("v1")
                .contact(new Contact().name("Kaleb Wondwossen Teshome").email("kaleb.wondwossen@bankofabyssinia.com"))
                .license(new License().name("Internal Use")))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
