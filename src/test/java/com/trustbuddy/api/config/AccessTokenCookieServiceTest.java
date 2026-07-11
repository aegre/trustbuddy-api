package com.trustbuddy.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.trustbuddy.api.config.properties.JwtCookieProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class AccessTokenCookieServiceTest {

		private AccessTokenCookieService accessTokenCookieService;

		@BeforeEach
		void setUp() {
				JwtCookieProperties cookieProperties =
								new JwtCookieProperties("access_token", false, "Lax");
				accessTokenCookieService =
								new AccessTokenCookieService(
												new com.trustbuddy.api.config.properties.JwtProperties(
																"test-jwt-secret-at-least-32-characters-long", 900_000L),
												cookieProperties);
		}

		@Test
		void givenBearerHeader_whenResolveToken_thenReturnBearerToken() {
				// Given
				MockHttpServletRequest request = new MockHttpServletRequest();
				request.addHeader("Authorization", "Bearer header-token");
				request.setCookies(new Cookie("access_token", "cookie-token"));

				// When
				var token = accessTokenCookieService.resolveToken(request);

				// Then
				assertThat(token).contains("header-token");
		}

		@Test
		void givenAccessTokenCookie_whenResolveToken_thenReturnCookieToken() {
				// Given
				MockHttpServletRequest request = new MockHttpServletRequest();
				request.setCookies(new Cookie("access_token", "cookie-token"));

				// When
				var token = accessTokenCookieService.resolveToken(request);

				// Then
				assertThat(token).contains("cookie-token");
		}

		@Test
		void givenNoCredentials_whenResolveToken_thenReturnEmpty() {
				// Given
				MockHttpServletRequest request = new MockHttpServletRequest();

				// When
				var token = accessTokenCookieService.resolveToken(request);

				// Then
				assertThat(token).isEmpty();
		}

		@Test
		void givenAccessToken_whenCreateAccessTokenCookie_thenHttpOnlyWithMaxAge() {
				// When
				var cookie = accessTokenCookieService.createAccessTokenCookie("jwt-value");

				// Then
				assertThat(cookie.isHttpOnly()).isTrue();
				assertThat(cookie.getValue()).isEqualTo("jwt-value");
				assertThat(cookie.getMaxAge().getSeconds()).isEqualTo(900L);
		}

		@Test
		void givenLogout_whenClearAccessTokenCookie_thenExpiresImmediately() {
				// When
				var cookie = accessTokenCookieService.clearAccessTokenCookie();

				// Then
				assertThat(cookie.getValue()).isEmpty();
				assertThat(cookie.getMaxAge().getSeconds()).isZero();
		}
}
