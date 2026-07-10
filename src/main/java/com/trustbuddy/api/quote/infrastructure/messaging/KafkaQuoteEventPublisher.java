package com.trustbuddy.api.quote.infrastructure.messaging;

import com.trustbuddy.api.quote.application.port.out.QuoteEventPublisherPort;
import com.trustbuddy.api.quote.domain.model.Quote;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(name = "quoteSubmittedProducerFactory")
public class KafkaQuoteEventPublisher implements QuoteEventPublisherPort {

		private final KafkaTemplate<String, QuoteSubmittedEvent> kafkaTemplate;
		private final String topic;

		public KafkaQuoteEventPublisher(
						ProducerFactory<String, QuoteSubmittedEvent> quoteSubmittedProducerFactory,
						@Value("${app.kafka.topic}") String topic) {
				this.kafkaTemplate = new KafkaTemplate<>(quoteSubmittedProducerFactory);
				this.topic = Objects.requireNonNull(topic, "topic");
		}

		@Override
		public void publishQuoteSubmitted(Quote quote) {
				QuoteSubmittedEvent event = QuoteSubmittedEvent.from(quote);
				kafkaTemplate.send(topic, quote.getId().toString(), event);
		}
}
