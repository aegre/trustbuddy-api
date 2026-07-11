package com.trustbuddy.api.quote.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.trustbuddy.api.config.JwtService;
import com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints;
import com.trustbuddy.api.quote.application.port.out.InsurerGatewayPort;
import com.trustbuddy.api.quote.application.port.out.InsurerSubmissionResult;
import com.trustbuddy.api.quote.application.port.out.QuoteEventPublisherPort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import com.trustbuddy.api.testsupport.PostgresRedisTestcontainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
				properties =
								"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class QuoteSubmitIT extends PostgresRedisTestcontainers {

		@Autowired private MockMvc mockMvc;

		@Autowired private JwtService jwtService;

		@Autowired private QuoteRepositoryPort quoteRepository;

		@MockitoBean private InsurerGatewayPort insurerGateway;

		@MockitoBean private QuoteEventPublisherPort quoteEventPublisher;

		@BeforeEach
		void stubInsurerGateway() {
				when(insurerGateway.submit(any())).thenReturn(new InsurerSubmissionResult(true, 200, "ok"));
		}

		@Test
		void givenReadyQuote_whenSubmitThroughApi_thenReturnsSubmittedQuote() throws Exception {
				// Given
				String token = bearerToken();
				String quoteId = createDraftQuote(token);
				updateCoverage(quoteId, token);

				// When / Then
				mockMvc.perform(
												post("/quotes/{id}/submit", quoteId)
																.header("Authorization", bearerAuthHeader(token)))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.status").value("SUBMITTED"))
								.andExpect(jsonPath("$.coverageType").value("STANDARD"))
								.andExpect(jsonPath("$.estimatedMonthlyPremium").exists());
		}

		@Test
		void givenDraftWithoutCoverage_whenSubmitThroughApi_thenReturns409() throws Exception {
				// Given
				String token = bearerToken();
				String quoteId = createDraftQuote(token);

				// When / Then
				mockMvc.perform(
												post("/quotes/{id}/submit", quoteId)
																.header("Authorization", bearerAuthHeader(token)))
								.andExpect(status().isConflict())
								.andExpect(jsonPath("$.status").value(409))
								.andExpect(
												jsonPath("$.message")
																.value("Quote is missing required coverage data"));
		}

		@Test
		void givenExpiredQuote_whenSubmitThroughApi_thenReturns409() throws Exception {
				// Given
				String token = bearerToken();
				var expired =
								quoteRepository.save(
												QuoteGenerator.readyForSubmission(30).withStatus(QuoteStatus.EXPIRED));

				// When / Then
				mockMvc.perform(
												post("/quotes/{id}/submit", expired.getId())
																.header("Authorization", bearerAuthHeader(token)))
								.andExpect(status().isConflict())
								.andExpect(jsonPath("$.status").value(409))
								.andExpect(
												jsonPath("$.message")
																.value("Cannot submit while quote is in status EXPIRED"));
		}

		private String bearerToken() {
				return jwtService.generateToken("test-user");
		}

		private static String bearerAuthHeader(String token) {
				return "Bearer " + token;
		}

		private String createDraftQuote(String token) throws Exception {
				MvcResult createResult =
								mockMvc.perform(
																post("/quotes")
																				.header("Authorization", bearerAuthHeader(token))
																				.contentType(MediaType.APPLICATION_JSON)
																				.content(
																								"""
								{
									"name": "Jane Doe",
									"email": "jane@example.com",
									"age": 30,
									"zipCode": "%s"
								}
								"""
																												.formatted(
																																QuoteFieldConstraints
																																				.ZIP_CODE_EXAMPLE)))
												.andExpect(status().isCreated())
												.andReturn();

				return JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
		}

		private void updateCoverage(String quoteId, String token) throws Exception {
				mockMvc.perform(
												patch("/quotes/{id}/coverage", quoteId)
																.header("Authorization", bearerAuthHeader(token))
																.contentType(MediaType.APPLICATION_JSON)
																.content(
																				"""
								{
									"coverageType": "STANDARD",
									"takesPrescriptionMedication": false,
									"usesTobacco": false,
									"needsSpouseCoverage": false
								}
								"""))
								.andExpect(status().isOk())
								.andExpect(jsonPath("$.coverageType").value("STANDARD"));
		}
}
