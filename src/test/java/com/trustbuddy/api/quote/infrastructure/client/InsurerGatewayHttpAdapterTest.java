package com.trustbuddy.api.quote.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.trustbuddy.api.quote.application.port.out.InsurerSubmissionResult;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class InsurerGatewayHttpAdapterTest {

		private static final String GATEWAY_URL = "https://httpstat.us/200";

		private MockRestServiceServer mockServer;
		private InsurerGatewayHttpAdapter adapter;

		@BeforeEach
		void setUp() {
				RestClient.Builder builder = RestClient.builder();
				mockServer = MockRestServiceServer.bindTo(builder).build();
				adapter = new InsurerGatewayHttpAdapter(builder.build(), GATEWAY_URL);
		}

		@Test
		void givenSuccessfulGatewayResponse_whenSubmit_thenReturnsSuccessResult() {
				// Given
				mockServer
								.expect(requestTo(GATEWAY_URL))
								.andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

				// When
				InsurerSubmissionResult result = adapter.submit(QuoteGenerator.draft(30));

				// Then
				assertThat(result.success()).isTrue();
				assertThat(result.httpStatus()).isEqualTo(200);
				mockServer.verify();
		}

		@Test
		void givenGatewayErrorResponse_whenSubmit_thenReturnsFailureResult() {
				// Given
				mockServer.expect(requestTo(GATEWAY_URL)).andRespond(withServerError());

				// When
				InsurerSubmissionResult result = adapter.submit(QuoteGenerator.draft(30));

				// Then
				assertThat(result.success()).isFalse();
				assertThat(result.httpStatus()).isEqualTo(500);
		}
}
