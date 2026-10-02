package com.fivesense.api.shared.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI fiveSenseOpenApi() {
        return new OpenAPI().info(new Info().title("Five Sense API").version("v1").description("API de avaliação de gerenciamento 5S; todas as rotas são públicas."));
    }
}
