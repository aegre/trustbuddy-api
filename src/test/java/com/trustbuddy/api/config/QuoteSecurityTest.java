package com.trustbuddy.api.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.trustbuddy.api.config.web.AuthController;
import com.trustbuddy.api.quote.application.service.QuoteService;
import com.trustbuddy.api.quote.application.service.QuoteSubmissionService;
import com.trustbuddy.api.quote.infrastructure.web.controller.QuoteController;
import com.trustbuddy.api.quote.infrastructure.web.exception.GlobalExceptionHandler;

@WebMvcTest(controllers = { QuoteController.class, AuthController.class })
@Import({
	ApplicationConfig.class,
	JwtService.class,
	JwtAuthFilter.class,
	JwtAuthenticationEntryPoint.class,
	SecurityConfig.class,
	GlobalExceptionHandler.class,
	JacksonAutoConfiguration.class,
	QuoteSecurityTest.CacheTestConfig.class
})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"app.jwt.secret=test-jwt-secret-at-least-32-characters-long",
		"app.jwt.expiration-ms=900000",
		"app.auth.username=test-user",
		"app.auth.password=test-password"
})
class QuoteSecurityTest {

	@TestConfiguration
	static class CacheTestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@MockitoBean
	private QuoteService quoteService;

	@MockitoBean
	private QuoteSubmissionService quoteSubmissionService;

	@Test
	void givenNoToken_whenListQuotes_thenReturn401() throws Exception {
		// Given — no Authorization header

		// When / Then
		mockMvc.perform(get("/quotes"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.path").value("/quotes"));
	}

	@Test
	void givenValidToken_whenListQuotes_thenReturn200() throws Exception {
		// Given
		String token = jwtService.generateToken("test-user");
		when(quoteService.listQuotes(any())).thenReturn(new PageImpl<>(java.util.List.of()));

		// When / Then
		mockMvc.perform(get("/quotes")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void givenInvalidToken_whenListQuotes_thenReturn401() throws Exception {
		// Given
		String token = jwtService.generateToken("test-user") + "invalid";

		// When / Then
		mockMvc.perform(get("/quotes")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void givenNoToken_whenAuthToken_thenReturn200() throws Exception {
		// Given
		String body = """
				{
				  "username": "test-user",
				  "password": "test-password"
				}
				""";

		// When / Then
		mockMvc.perform(post("/auth/token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}
}
