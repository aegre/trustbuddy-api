package com.trustbuddy.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.protocol.Request;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SentryScrubbingBeforeSendCallbackTest {

		private SentryScrubbingBeforeSendCallback callback;

		@BeforeEach
		void setUp() {
				callback = new SentryScrubbingBeforeSendCallback();
		}

		@Test
		void givenSensitiveHeaders_whenExecute_thenRedactAuthorizationAndCookie() {
				// Given
				Request request = new Request();
				Map<String, String> headers = new LinkedHashMap<>();
				headers.put("Authorization", "Bearer secret-jwt");
				headers.put("Cookie", "session=abc123");
				headers.put("Content-Type", "application/json");
				request.setHeaders(headers);

				SentryEvent event = new SentryEvent();
				event.setRequest(request);

				// When
				SentryEvent scrubbed = callback.execute(event, new Hint());

				// Then
				assertThat(scrubbed.getRequest().getHeaders())
								.containsEntry("Authorization", "[REDACTED]")
								.containsEntry("Cookie", "[REDACTED]")
								.containsEntry("Content-Type", "application/json");
		}

		@Test
		void givenSensitiveJsonBody_whenExecute_thenRedactMatchingFields() {
				// Given
				Request request = new Request();
				request.setData(
								"""
				{
					"username": "dev-user",
					"password": "dev-password",
					"accessToken": "jwt-value"
				}
				""");

				SentryEvent event = new SentryEvent();
				event.setRequest(request);

				// When
				SentryEvent scrubbed = callback.execute(event, new Hint());

				// Then
				String body = (String) scrubbed.getRequest().getData();
				assertThat(body).contains("\"username\": \"dev-user\"");
				assertThat(body).contains("\"password\": \"[REDACTED]\"");
				assertThat(body).contains("\"accessToken\": \"[REDACTED]\"");
				assertThat(body).doesNotContain("dev-password").doesNotContain("jwt-value");
		}

		@Test
		void givenNoRequest_whenExecute_thenReturnEventUnchanged() {
				// Given
				SentryEvent event = new SentryEvent();

				// When
				SentryEvent result = callback.execute(event, new Hint());

				// Then
				assertThat(result.getRequest()).isNull();
		}
}
