package com.trustbuddy.api.quote.infrastructure.messaging;

import com.trustbuddy.api.quote.application.port.out.QuoteEventPublisherPort;
import com.trustbuddy.api.quote.domain.model.Quote;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(QuoteEventPublisherPort.class)
public class NoOpQuoteEventPublisher implements QuoteEventPublisherPort {

		@Override
		public void publishQuoteSubmitted(Quote quote) {
				// Kafka is not configured in this runtime profile.
		}
}
