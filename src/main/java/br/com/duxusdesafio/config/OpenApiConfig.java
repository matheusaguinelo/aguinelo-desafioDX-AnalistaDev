package br.com.duxusdesafio.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da documentação OpenAPI/Swagger.
 *
 * <p>A UI interativa fica disponível em {@code /swagger-ui.html} após subir a aplicação.
 * Todos os endpoints são documentados automaticamente pelo springdoc-openapi.</p>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Duxus Challenge API",
                description = "Sistema de escalação de times — endpoints de cadastro e análise de dados.",
                version = "1.0.0"
        )
)
public class OpenApiConfig {
}
