package com.trustbuddy.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.security.SecurityScheme;

class OpenApiConfigTest {

	@Test
	void givenOpenApiConfig_whenTrustbuddyOpenApi_thenIncludesBearerJwtScheme() {
		// Given
		OpenApiConfig config = new OpenApiConfig();

		// When
		var openApi = config.trustbuddyOpenApi();

		// Then
		assertThat(openApi.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
		SecurityScheme scheme = openApi.getComponents().getSecuritySchemes().get("bearerAuth");
		assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
		assertThat(scheme.getScheme()).isEqualTo("bearer");
		assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
		assertThat(openApi.getSecurity()).anyMatch(requirement -> requirement.containsKey("bearerAuth"));
	}
}
