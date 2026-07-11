package com.trustbuddy.api.config.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trustbuddy.api.config.ApplicationConfig;
import com.trustbuddy.api.config.CorsConfig;
import com.trustbuddy.api.config.JwtService;
import com.trustbuddy.api.config.SecurityConfig;
import com.trustbuddy.api.config.SentryErrorReporter;
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
		CorsConfig.class,
		JwtService.class,
		SecurityConfig.class,
		GlobalExceptionHandler.class,
		SentryErrorReporter.class,
		JacksonAutoConfiguration.class,
		AuthControllerTest.CacheTestConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
				properties = {
						"app.jwt.secret=test-jwt-secret-at-least-32-characters-long",
						"app.jwt.expiration-ms=900000",
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
				mockMvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON).content(body))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.accessToken").isNotEmpty())
								.andExpect(jsonPath("$.tokenType").value("Bearer"))
								.andExpect(jsonPath("$.expiresInMs").value(900000));
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
				mockMvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON).content(body))
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
				mockMvc.perform(post("/auth/token").contentType(MediaType.APPLICATION_JSON).content(body))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400));
		}
}
