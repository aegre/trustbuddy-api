package com.trustbuddy.api.config.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access token issued after successful authentication")
public record AuthTokenResponse(
				@Schema(example = "eyJhbGciOiJIUzI1NiJ9...") String accessToken,
				@Schema(example = "Bearer") String tokenType,
				@Schema(example = "900000") long expiresInMs) {

		public static AuthTokenResponse bearer(String accessToken, long expiresInMs) {
				return new AuthTokenResponse(accessToken, "Bearer", expiresInMs);
		}
}
