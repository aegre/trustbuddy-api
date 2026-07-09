package com.trustbuddy.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.trustbuddy.api.config.properties.JwtProperties;

class JwtServiceTest {

	private static final String SECRET = "test-jwt-secret-at-least-32-characters-long";

	private final JwtService jwtService = new JwtService(new JwtProperties(SECRET, 900_000L));

	@Test
	void givenUsername_whenGenerateToken_thenTokenIsValidAndContainsSubject() {
		// Given
		String username = "test-user";

		// When
		String token = jwtService.generateToken(username);

		// Then
		assertThat(jwtService.isValid(token)).isTrue();
		assertThat(jwtService.extractUsername(token)).isEqualTo(username);
	}

	@Test
	void givenTamperedToken_whenIsValid_thenReturnFalse() {
		// Given
		String token = jwtService.generateToken("test-user") + "tampered";

		// When
		boolean valid = jwtService.isValid(token);

		// Then
		assertThat(valid).isFalse();
	}
}
