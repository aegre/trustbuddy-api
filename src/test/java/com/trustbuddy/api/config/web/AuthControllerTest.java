package com.trustbuddy.api.config.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trustbuddy.api.config.AccessTokenCookieService;
import com.trustbuddy.api.config.ApplicationConfig;
import com.trustbuddy.api.config.CorsConfig;
import com.trustbuddy.api.config.ErrorReportingConfig;
import com.trustbuddy.api.config.JwtService;
import com.trustbuddy.api.config.SecurityConfig;
import com.trustbuddy.api.config.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({
		ApplicationConfig.class,
		AccessTokenCookieService.class,
		CorsConfig.class,
		JwtService.class,
		SecurityConfig.class,
		GlobalExceptionHandler.class,
		ErrorReportingConfig.class,
		JacksonAutoConfiguration.class,
		AuthControllerTest.CacheTestConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
				properties = {
						"app.jwt.secret=test-jwt-secret-at-least-32-characters-long",
						"app.jwt.expiration-ms=900000",
						"app.jwt.cookie.name=access_token",
						"app.jwt.cookie.secure=false",
						"app.jwt.cookie.same-site=Lax",
						"app.auth.username=test-user",
						"app.auth.password=test-password",
						"app.cors.allowed-origins=http://localhost:5173"
				})
class AuthControllerTest {

		@TestConfiguration
		static class CacheTestConfig {

				@Bean
				CacheManager cacheManager() {
						return new ConcurrentMapCacheManager();
				}
		}

		@Autowired private MockMvc mockMvc;

		@Test
		void givenValidCredentials_whenToken_thenReturnBearerJwt() throws Exception {
				// Given
				String body =
								"""
				{
					"username": "test-user",
					"password": "test-password"
				}
				""";

				// When / Then
				mockMvc.perform(
												post(ApiPaths.AUTH + "/token")
																.contentType(MediaType.APPLICATION_JSON)
																.content(body))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.accessToken").isNotEmpty())
								.andExpect(jsonPath("$.tokenType").value("Bearer"))
								.andExpect(jsonPath("$.expiresInMs").value(900000))
								.andExpect(header().exists("Set-Cookie"))
								.andExpect(header().string("Set-Cookie", containsString("access_token=")))
								.andExpect(header().string("Set-Cookie", containsString("HttpOnly")));
		}

		@Test
		void givenAuthenticatedSession_whenLogout_thenClearsAccessTokenCookie() throws Exception {
				// When / Then
				mockMvc.perform(post(ApiPaths.AUTH + "/logout"))
								.andExpect(status().isNoContent())
								.andExpect(header().string("Set-Cookie", containsString("access_token=")))
								.andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
		}

		@Test
		void givenInvalidCredentials_whenToken_thenReturn401() throws Exception {
				// Given
				String body =
								"""
				{
					"username": "test-user",
					"password": "wrong-password"
				}
				""";

				// When / Then
				mockMvc.perform(
												post(ApiPaths.AUTH + "/token")
																.contentType(MediaType.APPLICATION_JSON)
																.content(body))
								.andExpect(status().isUnauthorized())
								.andExpect(jsonPath("$.status").value(401))
								.andExpect(jsonPath("$.message").value("Invalid username or password"));
		}

		@Test
		void givenBlankUsername_whenToken_thenReturn400() throws Exception {
				// Given
				String body =
								"""
				{
					"username": "",
					"password": "test-password"
				}
				""";

				// When / Then
				mockMvc.perform(
												post(ApiPaths.AUTH + "/token")
																.contentType(MediaType.APPLICATION_JSON)
																.content(body))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400));
		}
}
