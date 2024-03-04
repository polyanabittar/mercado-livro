package com.mercadolivro.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!prod")
class OpenApiConfig {
    @Bean
    fun springShopOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("Mercado Livro")
                    .description("API de controle de livros e clientes")
                    .version("v0.0.1")
            )
    }
}