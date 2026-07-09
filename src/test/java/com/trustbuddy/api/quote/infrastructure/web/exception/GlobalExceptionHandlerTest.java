package com.trustbuddy.api.quote.infrastructure.web.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(controllers = ExceptionTestController.class)
@Import({ GlobalExceptionHandler.class, GlobalExceptionHandlerTest.CacheTestConfig.class })
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

	@TestConfiguration
	static class CacheTestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void givenQuoteNotFound_whenRequest_thenReturns404ErrorResponse() throws Exception {
		// When / Then
		mockMvc.perform(get("/test/exceptions/quote-not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.error").value("Not Found"))
				.andExpect(jsonPath("$.message").value("Quote not found with id 00000000-0000-0000-0000-000000000001"))
				.andExpect(jsonPath("$.path").value("/test/exceptions/quote-not-found"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void givenInvalidQuoteState_whenRequest_thenReturns409ErrorResponse() throws Exception {
		// When / Then
		mockMvc.perform(get("/test/exceptions/invalid-state"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.error").value("Conflict"))
				.andExpect(jsonPath("$.message").value("Cannot update coverage while quote is in status SUBMITTED"));
	}

	@Test
	void givenConditionalFieldRejected_whenRequest_thenReturns400ErrorResponse() throws Exception {
		// When / Then
		mockMvc.perform(get("/test/exceptions/conditional-field"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value(
						"Supplemental health fields are not allowed when age is 65 or younger"));
	}

	@Test
	void givenQuoteValidation_whenRequest_thenReturns400ErrorResponse() throws Exception {
		// When / Then
		mockMvc.perform(get("/test/exceptions/quote-validation"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(
						"hasPreexistingConditions is required when age is over 65"));
	}

	@Test
	void givenExternalSubmissionFailure_whenRequest_thenReturns502ErrorResponse() throws Exception {
		// When / Then
		mockMvc.perform(get("/test/exceptions/external-submission"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.status").value(502))
				.andExpect(jsonPath("$.error").value("Bad Gateway"))
				.andExpect(jsonPath("$.message").value("Insurer gateway returned an error"));
	}

	@Test
	void givenInvalidRequestBody_whenPost_thenReturns400ValidationErrorResponse() throws Exception {
		// Given
		String invalidBody = """
				{
				  "name": "",
				  "email": "not-an-email",
				  "age": 0,
				  "zipCode": "ABCDE"
				}
				""";

		// When / Then
		mockMvc.perform(post("/test/exceptions/validation")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void givenAuthenticationFailure_whenRequest_thenReturns401ErrorResponse() throws Exception {
		// When / Then
		mockMvc.perform(get("/test/exceptions/authentication"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Unauthorized"))
				.andExpect(jsonPath("$.message").value("Invalid or missing token"));
	}

	@Test
	void givenAccessDenied_whenRequest_thenReturns403ErrorResponse() throws Exception {
		// When / Then
		mockMvc.perform(get("/test/exceptions/access-denied"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.error").value("Forbidden"))
				.andExpect(jsonPath("$.message").value("Access is denied"));
	}
}
