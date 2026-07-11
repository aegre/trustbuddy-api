package com.trustbuddy.api.config;

import com.trustbuddy.api.config.properties.JwtCookieProperties;
import com.trustbuddy.api.config.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Optional;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenCookieService {

		private static final String AUTHORIZATION_HEADER = "Authorization";
		private static final String BEARER_PREFIX = "Bearer ";

		private final JwtProperties jwtProperties;
		private final JwtCookieProperties jwtCookieProperties;

		public AccessTokenCookieService(JwtProperties jwtProperties, JwtCookieProperties jwtCookieProperties) {
				this.jwtProperties = jwtProperties;
				this.jwtCookieProperties = jwtCookieProperties;
		}

		public Optional<String> resolveToken(HttpServletRequest request) {
				Optional<String> bearerToken = resolveBearerToken(request.getHeader(AUTHORIZATION_HEADER));
				if (bearerToken.isPresent()) {
						return bearerToken;
				}
				return resolveCookieToken(request.getCookies());
		}

		public ResponseCookie createAccessTokenCookie(String accessToken) {
				return ResponseCookie.from(jwtCookieProperties.name(), accessToken)
								.httpOnly(true)
								.secure(jwtCookieProperties.secure())
								.sameSite(jwtCookieProperties.sameSite())
								.path("/")
								.maxAge(Duration.ofMillis(jwtProperties.expirationMs()))
								.build();
		}

		public ResponseCookie clearAccessTokenCookie() {
				return ResponseCookie.from(jwtCookieProperties.name(), "")
								.httpOnly(true)
								.secure(jwtCookieProperties.secure())
								.sameSite(jwtCookieProperties.sameSite())
								.path("/")
								.maxAge(Duration.ZERO)
								.build();
		}

		private static Optional<String> resolveBearerToken(String authorizationHeader) {
				if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
						return Optional.empty();
				}
				String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
				return token.isEmpty() ? Optional.empty() : Optional.of(token);
		}

		private Optional<String> resolveCookieToken(Cookie[] cookies) {
				if (cookies == null) {
						return Optional.empty();
				}
				for (Cookie cookie : cookies) {
						if (jwtCookieProperties.name().equals(cookie.getName())
										&& cookie.getValue() != null
										&& !cookie.getValue().isBlank()) {
								return Optional.of(cookie.getValue());
						}
				}
				return Optional.empty();
		}
}
