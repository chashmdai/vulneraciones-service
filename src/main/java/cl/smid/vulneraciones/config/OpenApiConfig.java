package cl.smid.vulneraciones.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI vulneracionesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SMID - Vulneraciones y FIR API")
                        .version("1.0.0")
                        .description("Gestión de Ficha Interna Reservada, vulneraciones y antecedentes reservados"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer emitido por smid-auth.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
