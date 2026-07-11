package com.trustbuddy.api.quote.infrastructure.web.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trustbuddy.api.config.ErrorReportingConfig;
import com.trustbuddy.api.quote.domain.exception.QuoteErrorCodes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ExceptionTestController.class)
@Import({
		QuoteExceptionHandler.class,
		ErrorReportingConfig.class,
		QuoteExceptionHandlerTest.CacheTestConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
class QuoteExceptionHandlerTest {

		@TestConfiguration
		static class CacheTestConfig {

				@Bean
				CacheManager cacheManager() {
						return new ConcurrentMapCacheManager();
				}
		}

		@Autowired private MockMvc mockMvc;

		@Test
		void givenQuoteNotFound_whenRequest_thenReturns404ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/exceptions/quote-not-found"))
								.andExpect(status().isNotFound())
								.andExpect(jsonPath("$.status").value(404))
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_NOT_FOUND))
								.andExpect(jsonPath("$.error").value("Not Found"))
								.andExpect(
												jsonPath("$.message")
																.value(
																				"Quote not found with id 00000000-0000-0000-0000-000000000001"))
								.andExpect(jsonPath("$.path").value("/test/exceptions/quote-not-found"))
								.andExpect(jsonPath("$.timestamp").exists());
		}

		@Test
		void givenInvalidQuoteState_whenRequest_thenReturns409ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/exceptions/invalid-state"))
								.andExpect(status().isConflict())
								.andExpect(jsonPath("$.status").value(409))
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_INVALID_STATUS))
								.andExpect(jsonPath("$.error").value("Conflict"))
								.andExpect(
												jsonPath("$.message")
																.value(
																				"Cannot update coverage while quote is in status SUBMITTED"));
		}

		@Test
		void givenConditionalFieldRejected_whenRequest_thenReturns400ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/exceptions/conditional-field"))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400))
								.andExpect(
												jsonPath("$.code").value(QuoteErrorCodes.QUOTE_CONDITIONAL_FIELD_REJECTED))
								.andExpect(
												jsonPath("$.message")
																.value(
																				"Supplemental health fields are not allowed when age is 65 or younger"));
		}

		@Test
		void givenQuoteValidation_whenRequest_thenReturns400ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/exceptions/quote-validation"))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_VALIDATION_FAILED))
								.andExpect(
												jsonPath("$.message")
																.value("hasPreexistingConditions is required when age is over 65"));
		}

		@Test
		void givenExternalSubmissionFailure_whenRequest_thenReturns502ErrorResponse() throws Exception {
				// When / Then
				mockMvc.perform(get("/test/exceptions/external-submission"))
								.andExpect(status().isBadGateway())
								.andExpect(jsonPath("$.status").value(502))
								.andExpect(
												jsonPath("$.code").value(QuoteErrorCodes.QUOTE_EXTERNAL_SUBMISSION_FAILED))
								.andExpect(jsonPath("$.error").value("Bad Gateway"))
								.andExpect(jsonPath("$.message").value("Insurer gateway returned an error"));
		}
}
