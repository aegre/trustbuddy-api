package com.trustbuddy.api.quote.infrastructure.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trustbuddy.api.config.ErrorReportingConfig;
import com.trustbuddy.api.config.web.ApiPaths;
import com.trustbuddy.api.config.web.exception.ClientRequestExceptionHandler;
import com.trustbuddy.api.config.web.exception.GlobalExceptionHandler;
import com.trustbuddy.api.config.web.response.ApiErrorCodes;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.application.service.QuoteService;
import com.trustbuddy.api.quote.application.service.QuoteSubmissionService;
import com.trustbuddy.api.quote.application.validation.CommandValidator;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteErrorCodes;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.web.exception.QuoteExceptionHandler;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = QuoteController.class)
@Import({
		GlobalExceptionHandler.class,
		ClientRequestExceptionHandler.class,
		QuoteExceptionHandler.class,
		ErrorReportingConfig.class,
		CommandValidator.class,
		QuoteControllerTest.TestConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
class QuoteControllerTest {

		@TestConfiguration
		static class TestConfig {

				@Bean
				CacheManager cacheManager() {
						return new ConcurrentMapCacheManager();
				}

				@Bean
				QuoteCachePort quoteCachePort() {
						return org.mockito.Mockito.mock(QuoteCachePort.class);
				}

				@Bean
				QuoteService quoteService(
								QuoteRepositoryPort quoteRepository,
								QuoteCachePort quoteCachePort,
								CommandValidator commandValidator) {
						return new QuoteService(quoteRepository, quoteCachePort, commandValidator);
				}
		}

		@Autowired private MockMvc mockMvc;

		@MockitoBean private QuoteRepositoryPort quoteRepository;

		@MockitoBean private QuoteSubmissionService quoteSubmissionService;

		@Test
		void givenValidRequest_whenCreateQuote_thenReturns201WithLocation() throws Exception {
				// Given
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When / Then
				mockMvc.perform(
												post(ApiPaths.QUOTES)
																.contentType(MediaType.APPLICATION_JSON)
																.content(
																				"""
								{
									"name": "Jane Doe",
									"email": "jane@example.com",
									"age": 30,
									"zipCode": "06600"
								}
								"""))
								.andExpect(status().isCreated())
								.andExpect(header().exists("Location"))
								.andExpect(jsonPath("$.id").exists())
								.andExpect(jsonPath("$.status").value("DRAFT"));
		}

		@Test
		void givenInvalidCreateRequest_whenCreateQuote_thenReturns400() throws Exception {
				// When / Then
				mockMvc.perform(
												post(ApiPaths.QUOTES)
																.contentType(MediaType.APPLICATION_JSON)
																.content(
																				"""
								{
									"name": "",
									"email": "not-an-email",
									"age": 0,
									"zipCode": "abc"
								}
								"""))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400));

				verify(quoteRepository, never()).save(any());
		}

		@Test
		void givenDraftQuote_whenUpdatePersonalInfo_thenReturnsUpdatedQuote() throws Exception {
				// Given
				var draft = QuoteGenerator.draft(30);
				when(quoteRepository.findById(draft.getId())).thenReturn(java.util.Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When / Then
				mockMvc.perform(
												patch(ApiPaths.QUOTES + "/{id}", draft.getId())
																.contentType(MediaType.APPLICATION_JSON)
																.content(
																				"""
								{
									"name": "Jane Updated",
									"email": "updated@example.com",
									"age": 35,
									"zipCode": "06600"
								}
								"""))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.id").value(draft.getId().toString()))
								.andExpect(jsonPath("$.name").value("Jane Updated"))
								.andExpect(jsonPath("$.email").value("updated@example.com"))
								.andExpect(jsonPath("$.age").value(35));
		}

		@Test
		void givenSubmittedQuote_whenUpdatePersonalInfo_thenReturns409() throws Exception {
				// Given
				var submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);
				when(quoteRepository.findById(submitted.getId()))
								.thenReturn(java.util.Optional.of(submitted));

				// When / Then
				mockMvc.perform(
												patch(ApiPaths.QUOTES + "/{id}", submitted.getId())
																.contentType(MediaType.APPLICATION_JSON)
																.content(
																				"""
								{
									"name": "Jane Doe",
									"email": "jane@example.com",
									"age": 30,
									"zipCode": "06600"
								}
								"""))
								.andExpect(status().isConflict())
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_INVALID_STATUS));

				verify(quoteRepository, never()).save(any());
		}

		@Test
		void givenCoveredQuote_whenSubmitQuote_thenReturnsSubmittedQuote() throws Exception {
				// Given
				var quote =
								QuoteGenerator.coverage(30, CoverageType.STANDARD)
												.build()
												.withStatus(QuoteStatus.SUBMITTED);
				when(quoteSubmissionService.submitQuote(quote.getId())).thenReturn(quote);

				// When / Then
				mockMvc.perform(post(ApiPaths.QUOTES + "/{id}/submit", quote.getId()))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.status").value("SUBMITTED"))
								.andExpect(jsonPath("$.coverageType").value("STANDARD"));
		}

		@Test
		void givenDraftWithoutCoverage_whenSubmitQuote_thenReturns409() throws Exception {
				// Given
				var quote = QuoteGenerator.draft(30);
				when(quoteSubmissionService.submitQuote(quote.getId()))
								.thenThrow(
												new InvalidQuoteStateException(
																QuoteErrorCodes.QUOTE_MISSING_COVERAGE,
																"Quote is missing required coverage data"));

				// When / Then
				mockMvc.perform(post(ApiPaths.QUOTES + "/{id}/submit", quote.getId()))
								.andExpect(status().isConflict())
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_MISSING_COVERAGE))
								.andExpect(jsonPath("$.message").value("Quote is missing required coverage data"));
		}

		@Test
		void givenInvalidUuid_whenGetQuote_thenReturns400() throws Exception {
				// When / Then
				mockMvc.perform(get(ApiPaths.QUOTES + "/{id}", "not-a-uuid"))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400))
								.andExpect(jsonPath("$.code").value(ApiErrorCodes.INVALID_REQUEST))
								.andExpect(jsonPath("$.message").value("id must be a valid UUID"));

				verify(quoteRepository, never()).findById(any());
		}

		@Test
		void givenUnknownQuote_whenGetQuote_thenReturns404() throws Exception {
				// Given
				UUID id = UUID.randomUUID();
				when(quoteRepository.findById(id)).thenReturn(java.util.Optional.empty());

				// When / Then
				mockMvc.perform(get(ApiPaths.QUOTES + "/{id}", id))
								.andExpect(status().isNotFound())
								.andExpect(jsonPath("$.message").value("Quote not found with id " + id));
		}

		@Test
		void givenPartialCoverageRequest_whenUpdateCoverage_thenReturnsUpdatedQuote() throws Exception {
				// Given
				var draft =
								QuoteGenerator.coverage(30, CoverageType.STANDARD)
												.usesTobacco(false)
												.needsSpouseCoverage(false)
												.build();
				when(quoteRepository.findById(draft.getId())).thenReturn(java.util.Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When / Then
				mockMvc.perform(
												patch(ApiPaths.QUOTES + "/{id}/coverage", draft.getId())
																.contentType(MediaType.APPLICATION_JSON)
																.content(
																				"""
								{
									"usesTobacco": true
								}
								"""))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.coverageType").value("STANDARD"))
								.andExpect(jsonPath("$.usesTobacco").value(true));
		}

		@Test
		void givenCoverageRequest_whenUpdateCoverage_thenReturnsUpdatedQuote() throws Exception {
				// Given
				var draft = QuoteGenerator.draft(30);
				when(quoteRepository.findById(draft.getId())).thenReturn(java.util.Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When / Then
				mockMvc.perform(
												patch(ApiPaths.QUOTES + "/{id}/coverage", draft.getId())
																.contentType(MediaType.APPLICATION_JSON)
																.content(
																				"""
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
				when(quoteRepository.findAll(any())).thenReturn(new PageImpl<>(java.util.List.of(quote)));

				// When / Then
				mockMvc.perform(get(ApiPaths.QUOTES))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.content[0].id").value(quote.getId().toString()));
		}

		@Test
		void givenInvalidSortField_whenListQuotes_thenReturns400() throws Exception {
				// When / Then
				mockMvc.perform(get(ApiPaths.QUOTES).param("sort", "unknownField,asc"))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400))
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_INVALID_QUERY))
								.andExpect(
												jsonPath("$.message")
																.value(
																				"Invalid sort field 'unknownField'. Use sort=<field>,asc|desc. Allowed fields: age, createdAt, email, name, status, updatedAt"));

				verify(quoteRepository, never()).findAll(any());
		}

		@Test
		void givenInvalidSortFormat_whenListQuotes_thenReturns400() throws Exception {
				// When / Then
				mockMvc.perform(get(ApiPaths.QUOTES).param("sort", "age,createdAt"))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400))
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_INVALID_QUERY))
								.andExpect(
												jsonPath("$.message")
																.value(
																				"Invalid sort format 'age,createdAt'. Use sort=<field>,asc|desc for each sort parameter. Allowed fields: age, createdAt, email, name, status, updatedAt"));

				verify(quoteRepository, never()).findAll(any());
		}

		@Test
		void givenDirectionFirstSortFormat_whenListQuotes_thenReturns400() throws Exception {
				// When / Then
				mockMvc.perform(get(ApiPaths.QUOTES).param("sort", "asc,age,createdAt"))
								.andExpect(status().isBadRequest())
								.andExpect(jsonPath("$.status").value(400))
								.andExpect(jsonPath("$.code").value(QuoteErrorCodes.QUOTE_INVALID_QUERY))
								.andExpect(
												jsonPath("$.message")
																.value(
																				"Invalid sort format 'asc,age,createdAt'. Use sort=<field>,asc|desc for each sort parameter. Allowed fields: age, createdAt, email, name, status, updatedAt"));

				verify(quoteRepository, never()).findAll(any());
		}

		@Test
		void givenMultipleSortParams_whenListQuotes_thenReturn200() throws Exception {
				// Given
				var quote = QuoteGenerator.draft(30);
				when(quoteRepository.findAll(any())).thenReturn(new PageImpl<>(java.util.List.of(quote)));

				// When / Then
				mockMvc.perform(
												get(ApiPaths.QUOTES)
																.param("sort", "status,asc")
																.param("sort", "createdAt,desc"))
								.andExpect(status().isOk());

				ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
				verify(quoteRepository).findAll(pageableCaptor.capture());
				assertThat(pageableCaptor.getValue().getSort().toList())
								.containsExactly(Sort.Order.asc("status"), Sort.Order.desc("createdAt"));
		}

		@Test
		void givenExcessivePageSize_whenListQuotes_thenClampSizeTo100() throws Exception {
				// Given
				var quote = QuoteGenerator.draft(30);
				when(quoteRepository.findAll(any())).thenReturn(new PageImpl<>(java.util.List.of(quote)));

				// When / Then
				mockMvc.perform(get(ApiPaths.QUOTES).param("size", "101")).andExpect(status().isOk());

				ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
				verify(quoteRepository).findAll(pageableCaptor.capture());
				assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
		}
}
