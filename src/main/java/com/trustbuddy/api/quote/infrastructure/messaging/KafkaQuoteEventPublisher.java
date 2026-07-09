package com.trustbuddy.api.quote.infrastructure.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.trustbuddy.api.quote.application.port.out.QuoteEventPublisherPort;
import com.trustbuddy.api.quote.domain.model.Quote;

@Component
@ConditionalOnBean(name = "quoteSubmittedKafkaTemplate")
public class KafkaQuoteEventPublisher implements QuoteEventPublisherPort {

	private final KafkaTemplate<String, QuoteSubmittedEvent> kafkaTemplate;
	private final String topic;

	public KafkaQuoteEventPublisher(
			KafkaTemplate<String, QuoteSubmittedEvent> kafkaTemplate,
			@Value("${app.kafka.topic}") String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Override
	public void publishQuoteSubmitted(Quote quote) {
		QuoteSubmittedEvent event = QuoteSubmittedEvent.from(quote);
		kafkaTemplate.send(topic, quote.getId().toString(), event);
	}
}
