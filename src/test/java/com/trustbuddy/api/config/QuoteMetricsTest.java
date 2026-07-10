package com.trustbuddy.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class QuoteMetricsTest {

		@Test
		void givenRegistry_whenRecordEvents_thenIncrementsCounters() {
				// Given
				SimpleMeterRegistry registry = new SimpleMeterRegistry();
				QuoteMetrics metrics = new QuoteMetrics(registry);

				// When
				metrics.recordSubmission();
				metrics.recordSubmissionFailed();
				metrics.recordExpired(3);

				// Then
				assertThat(registry.get("quote.submissions.total").counter().count()).isEqualTo(1);
				assertThat(registry.get("quote.submissions.failed").counter().count()).isEqualTo(1);
				assertThat(registry.get("quote.expired.total").counter().count()).isEqualTo(3);
		}

		@Test
		void givenZeroExpiredCount_whenRecordExpired_thenDoesNotIncrement() {
				// Given
				SimpleMeterRegistry registry = new SimpleMeterRegistry();
				QuoteMetrics metrics = new QuoteMetrics(registry);

				// When
				metrics.recordExpired(0);

				// Then
				assertThat(registry.find("quote.expired.total").counter()).isNull();
		}
}
