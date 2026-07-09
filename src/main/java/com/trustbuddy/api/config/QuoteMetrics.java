package com.trustbuddy.api.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class QuoteMetrics {

	private final Counter submissionsTotal;
	private final Counter submissionsFailed;
	private final ExpiredCounter expiredCounter;

	public QuoteMetrics(MeterRegistry meterRegistry) {
		this.submissionsTotal = Counter.builder("quote.submissions.total")
				.description("Successful first-time quote submissions")
				.register(meterRegistry);
		this.submissionsFailed = Counter.builder("quote.submissions.failed")
				.description("Quote submissions that failed at the insurer gateway")
				.register(meterRegistry);
		this.expiredCounter = new ExpiredCounter(meterRegistry);
	}

	public static QuoteMetrics noop() {
		SimpleMeterRegistry registry = new SimpleMeterRegistry();
		return new QuoteMetrics(registry);
	}

	public void recordSubmission() {
		submissionsTotal.increment();
	}

	public void recordSubmissionFailed() {
		submissionsFailed.increment();
	}

	public void recordExpired(int count) {
		expiredCounter.record(count);
	}

	private static final class ExpiredCounter {

		private final MeterRegistry registry;
		private Counter counter;

		private ExpiredCounter(MeterRegistry registry) {
			this.registry = registry;
		}

		void record(int count) {
			if (count > 0) {
				if (counter == null) {
					counter = Counter.builder("quote.expired.total")
							.description("Draft quotes expired by the scheduled job")
							.register(registry);
				}
				counter.increment(count);
			}
		}
	}
}
