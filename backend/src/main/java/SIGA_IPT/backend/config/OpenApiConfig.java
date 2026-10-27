package SIGA_IPT.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String KEYCLOAK_SCHEME = "keycloakLogin";

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public OpenAPI sigaOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("SIGA IPT API")
                        .description("Academic management API of the Instituto Profesional de Tecnologia.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(KEYCLOAK_SCHEME, keycloakLoginScheme())
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(KEYCLOAK_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    private SecurityScheme keycloakLoginScheme(){
        OAuthFlow authorizationCode = new OAuthFlow()
                .authorizationUrl(issuerUri + "/protocol/openid-connect/auth")
                .tokenUrl(issuerUri + "/protocol/openid-connect/token")
                .scopes(new Scopes().addString("openid", "OpenID Connect"));
        return new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().authorizationCode(authorizationCode));
    }
}
