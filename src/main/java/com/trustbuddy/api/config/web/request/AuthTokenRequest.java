package com.trustbuddy.api.config.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials used to obtain a JWT access token")
public record AuthTokenRequest(
		@NotBlank @Schema(example = "dev-user") String username,
		@NotBlank @Schema(example = "dev-password") String password) {
}
