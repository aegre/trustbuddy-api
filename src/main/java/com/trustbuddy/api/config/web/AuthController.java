package com.trustbuddy.api.config.web;

import com.trustbuddy.api.config.AccessTokenCookieService;
import com.trustbuddy.api.config.JwtService;
import com.trustbuddy.api.config.properties.AuthProperties;
import com.trustbuddy.api.config.properties.JwtProperties;
import com.trustbuddy.api.config.web.request.AuthTokenRequest;
import com.trustbuddy.api.config.web.response.AuthTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
@Tag(name = "Authentication")
public class AuthController {

		private final JwtService jwtService;
		private final JwtProperties jwtProperties;
		private final AuthProperties authProperties;
		private final AccessTokenCookieService accessTokenCookieService;

		public AuthController(
						JwtService jwtService,
						JwtProperties jwtProperties,
						AuthProperties authProperties,
						AccessTokenCookieService accessTokenCookieService) {
				this.jwtService = jwtService;
				this.jwtProperties = jwtProperties;
				this.authProperties = authProperties;
				this.accessTokenCookieService = accessTokenCookieService;
		}

		@PostMapping("/token")
		@Operation(
						summary = "Obtain a JWT access token",
						description =
										"Returns the JWT in the response body for Bearer clients and sets an HttpOnly"
														+ " access-token cookie for browser clients.")
		@SecurityRequirements
		public ResponseEntity<AuthTokenResponse> token(@Valid @RequestBody AuthTokenRequest request) {
				if (!isValidCredentials(request)) {
						throw new BadCredentialsException("Invalid username or password");
				}
				String accessToken = jwtService.generateToken(request.username());
				return ResponseEntity.ok()
								.header(
												HttpHeaders.SET_COOKIE,
												accessTokenCookieService.createAccessTokenCookie(accessToken).toString())
								.body(AuthTokenResponse.bearer(accessToken, jwtProperties.expirationMs()));
		}

		@PostMapping("/logout")
		@Operation(
						summary = "Clear the access-token cookie",
						description =
										"Clears the HttpOnly JWT cookie. Bearer clients discard the token client-side.")
		@SecurityRequirements
		public ResponseEntity<Void> logout() {
				return ResponseEntity.noContent()
								.header(
												HttpHeaders.SET_COOKIE,
												accessTokenCookieService.clearAccessTokenCookie().toString())
								.build();
		}

		private boolean isValidCredentials(AuthTokenRequest request) {
				return authProperties.username().equals(request.username())
								&& authProperties.password().equals(request.password());
		}
}
