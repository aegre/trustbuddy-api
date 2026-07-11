package com.trustbuddy.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

		private static final String BEARER_AUTH_SCHEME = "bearerAuth";

		@Bean
		OpenAPI trustbuddyOpenApi() {
				return new OpenAPI()
								.info(
												new Info()
																.title("Trustbuddy API")
																.version("v1")
																.description("Insurance quote API for Trustbuddy"))
								.components(
												new Components()
																.addSecuritySchemes(
																				BEARER_AUTH_SCHEME,
																				new SecurityScheme()
																								.type(SecurityScheme.Type.HTTP)
																								.scheme("bearer")
																								.bearerFormat("JWT")
																								.description(
																												"JWT from POST /auth/token — send as Authorization Bearer header"
																																+ " or via HttpOnly access_token cookie for browser clients")))
								.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
		}
}
