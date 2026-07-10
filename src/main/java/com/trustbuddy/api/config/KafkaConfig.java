package com.trustbuddy.api.config;

import com.trustbuddy.api.quote.infrastructure.messaging.QuoteSubmittedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers", matchIfMissing = false)
public class KafkaConfig {

		@Bean
		ProducerFactory<String, QuoteSubmittedEvent> quoteSubmittedProducerFactory(
						@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
				Map<String, Object> config = new HashMap<>();
				config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
				config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
				config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
				config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
				return new DefaultKafkaProducerFactory<>(
								config, new StringSerializer(), new JsonSerializer<QuoteSubmittedEvent>());
		}

		@Bean
		KafkaTemplate<String, QuoteSubmittedEvent> quoteSubmittedKafkaTemplate(
						ProducerFactory<String, QuoteSubmittedEvent> quoteSubmittedProducerFactory) {
				return new KafkaTemplate<>(quoteSubmittedProducerFactory);
		}
}
