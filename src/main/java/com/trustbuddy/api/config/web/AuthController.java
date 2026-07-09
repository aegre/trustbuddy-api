package com.trustbuddy.api.config.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trustbuddy.api.config.JwtService;
import com.trustbuddy.api.config.properties.AuthProperties;
import com.trustbuddy.api.config.properties.JwtProperties;
import com.trustbuddy.api.config.web.request.AuthTokenRequest;
import com.trustbuddy.api.config.web.response.AuthTokenResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication")
public class AuthController {

	private final JwtService jwtService;
	private final JwtProperties jwtProperties;
	private final AuthProperties authProperties;

	public AuthController(JwtService jwtService, JwtProperties jwtProperties, AuthProperties authProperties) {
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
		this.authProperties = authProperties;
	}

	@PostMapping("/token")
	@Operation(summary = "Obtain a JWT access token")
	@SecurityRequirements
	public ResponseEntity<AuthTokenResponse> token(@Valid @RequestBody AuthTokenRequest request) {
		if (!isValidCredentials(request)) {
			throw new BadCredentialsException("Invalid username or password");
		}
		String accessToken = jwtService.generateToken(request.username());
		return ResponseEntity.ok(AuthTokenResponse.bearer(accessToken, jwtProperties.expirationMs()));
	}

	private boolean isValidCredentials(AuthTokenRequest request) {
		return authProperties.username().equals(request.username())
				&& authProperties.password().equals(request.password());
	}
}
