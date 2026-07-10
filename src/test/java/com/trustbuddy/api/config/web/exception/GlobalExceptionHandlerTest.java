package com.trustbuddy.api.config.web.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trustbuddy.api.config.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GlobalExceptionTestController.class)
@Import({
		GlobalExceptionHandler.class,
		GlobalExceptionHandlerTest.CacheTestConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

		@TestConfiguration
		static class CacheTestConfig {

				@Bean
				CacheManager cacheManager() {
						return new ConcurrentMapCacheManager();
				}
		}

		@Autowired private MockMvc mockMvc;

		@Test
		void givenInvalidRequestBody_whenPost_thenReturns400ValidationErrorResponse() throws Exception {
				// Given
				String invalidBody =
								"""
				{
					"name": "",
					"email": "not-an-email"
				}
				""";

				// When / Then
				mockMvc.perform(
												post("/test/global-exceptions/validation")
																.contentType(MediaType.APPLICATION_JSON)
																.content(invalidBody))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400))
								.andExpect(jsonPath("$.message").exists());
		}

		@Test
		void givenAuthenticationFailure_whenRequest_thenReturns401ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/global-exceptions/authentication"))
								.andExpect(status().isUnauthorized())
								.andExpect(jsonPath("$.status").value(401))
								.andExpect(jsonPath("$.error").value("Unauthorized"))
								.andExpect(jsonPath("$.message").value("Invalid or missing token"));
		}

		@Test
		void givenAccessDenied_whenRequest_thenReturns403ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/global-exceptions/access-denied"))
								.andExpect(status().isForbidden())
								.andExpect(jsonPath("$.status").value(403))
								.andExpect(jsonPath("$.error").value("Forbidden"))
								.andExpect(jsonPath("$.message").value("Access is denied"));
		}

		@Test
		void givenUnexpectedException_whenRequest_thenReturns500ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/global-exceptions/unexpected"))
								.andExpect(status().isInternalServerError())
								.andExpect(jsonPath("$.status").value(500))
								.andExpect(jsonPath("$.error").value("Internal Server Error"))
								.andExpect(jsonPath("$.message").value("An unexpected error occurred"))
								.andExpect(jsonPath("$.path").value("/test/global-exceptions/unexpected"));
		}
}
