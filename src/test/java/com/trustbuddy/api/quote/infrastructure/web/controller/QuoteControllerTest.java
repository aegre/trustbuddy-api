package com.trustbuddy.api.quote.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.trustbuddy.api.quote.application.service.QuoteService;
import com.trustbuddy.api.quote.application.service.QuoteSubmissionService;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.web.exception.GlobalExceptionHandler;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;

@WebMvcTest(controllers = QuoteController.class)
@Import({ GlobalExceptionHandler.class, QuoteControllerTest.CacheTestConfig.class })
@AutoConfigureMockMvc(addFilters = false)
class QuoteControllerTest {

	@TestConfiguration
	static class CacheTestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private QuoteService quoteService;

	@MockitoBean
	private QuoteSubmissionService quoteSubmissionService;

	@Test
	void givenValidRequest_whenCreateQuote_thenReturns201WithLocation() throws Exception {
		// Given
		var quote = QuoteGenerator.draft(30);
		when(quoteService.createQuote("Jane Doe", "jane@example.com", 30, "12345")).thenReturn(quote);

		// When / Then
		mockMvc.perform(post("/quotes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Jane Doe",
								  "email": "jane@example.com",
								  "age": 30,
								  "zipCode": "12345"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/quotes/" + quote.getId()))
				.andExpect(jsonPath("$.id").value(quote.getId().toString()))
				.andExpect(jsonPath("$.status").value("DRAFT"));
	}

	@Test
	void givenInvalidCreateRequest_whenCreateQuote_thenReturns400() throws Exception {
		// When / Then
		mockMvc.perform(post("/quotes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "",
								  "email": "not-an-email",
								  "age": 0,
								  "zipCode": "abc"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		verify(quoteService, never()).createQuote(any(), any(), any(Integer.class), any());
	}

	@Test
	void givenCoveredQuote_whenSubmitQuote_thenReturnsSubmittedQuote() throws Exception {
		// Given
		var quote = QuoteGenerator.coverage(30, CoverageType.STANDARD)
				.build()
				.withStatus(QuoteStatus.SUBMITTED);
		when(quoteSubmissionService.submitQuote(quote.getId())).thenReturn(quote);

		// When / Then
		mockMvc.perform(post("/quotes/{id}/submit", quote.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUBMITTED"))
				.andExpect(jsonPath("$.coverageType").value("STANDARD"));
	}

	@Test
	void givenDraftWithoutCoverage_whenSubmitQuote_thenReturns400() throws Exception {
		// Given
		var quote = QuoteGenerator.draft(30);
		when(quoteSubmissionService.submitQuote(quote.getId()))
				.thenThrow(new QuoteValidationException("Quote is missing required coverage data"));

		// When / Then
		mockMvc.perform(post("/quotes/{id}/submit", quote.getId()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Quote is missing required coverage data"));
	}

	@Test
	void givenUnknownQuote_whenGetQuote_thenReturns404() throws Exception {
		// Given
		UUID id = UUID.randomUUID();
		when(quoteService.getQuote(id)).thenThrow(new QuoteNotFoundException(id));

		// When / Then
		mockMvc.perform(get("/quotes/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Quote not found with id " + id));
	}

	@Test
	void givenCoverageRequest_whenUpdateCoverage_thenReturnsUpdatedQuote() throws Exception {
		// Given
		var quote = QuoteGenerator.coverage(30, CoverageType.PREMIUM).build();
		when(quoteService.updateCoverage(
				eq(quote.getId()),
				eq(CoverageType.PREMIUM),
				eq(null),
				eq(null),
				eq(null),
				eq(null),
				eq(null))).thenReturn(quote);

		// When / Then
		mockMvc.perform(patch("/quotes/{id}/coverage", quote.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "coverageType": "PREMIUM"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coverageType").value("PREMIUM"));
	}

	@Test
	void givenQuotes_whenListQuotes_thenReturnsPage() throws Exception {
		// Given
		var quote = QuoteGenerator.draft(30);
		when(quoteService.listQuotes(PageRequest.of(0, 20)))
				.thenReturn(new PageImpl<>(java.util.List.of(quote)));

		// When / Then
		mockMvc.perform(get("/quotes"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(quote.getId().toString()));
	}
}
