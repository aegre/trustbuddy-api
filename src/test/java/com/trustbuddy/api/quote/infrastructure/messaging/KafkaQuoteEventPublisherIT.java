package com.trustbuddy.api.quote.infrastructure.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class KafkaQuoteEventPublisherIT {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

	@Container
	@ServiceConnection
	static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

	@Container
	@ServiceConnection
	static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.0"));

	@Autowired
	private KafkaQuoteEventPublisher quoteEventPublisher;

	@Value("${app.kafka.topic}")
	private String topic;

	@Test
	void givenSubmittedQuote_whenPublishQuoteSubmitted_thenEventIsPublishedToTopic() {
		// Given
		Quote submitted = QuoteGenerator.readyForSubmission(30).withStatus(QuoteStatus.SUBMITTED);

		// When
		quoteEventPublisher.publishQuoteSubmitted(submitted);

		// Then
		try (KafkaConsumer<String, QuoteSubmittedEvent> consumer = createConsumer()) {
			consumer.subscribe(List.of(topic));
			ConsumerRecord<String, QuoteSubmittedEvent> record = pollForRecord(consumer);
			QuoteSubmittedEvent event = record.value();

			assertThat(record.key()).isEqualTo(submitted.getId().toString());
			assertThat(event.quoteId()).isEqualTo(submitted.getId());
			assertThat(event.status()).isEqualTo(QuoteStatus.SUBMITTED);
			assertThat(event.coverageType()).isEqualTo(submitted.getCoverageType());
			assertThat(event.premium()).isEqualByComparingTo(submitted.getEstimatedMonthlyPremium());
			assertThat(event.timestamp()).isNotNull();
		}
	}

	private KafkaConsumer<String, QuoteSubmittedEvent> createConsumer() {
		Properties properties = new Properties();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "quote-submitted-it-" + UUID.randomUUID());
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
		properties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.trustbuddy.api.quote.infrastructure.messaging");
		properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, QuoteSubmittedEvent.class.getName());
		return new KafkaConsumer<>(properties);
	}

	private ConsumerRecord<String, QuoteSubmittedEvent> pollForRecord(
			KafkaConsumer<String, QuoteSubmittedEvent> consumer) {
		long deadline = System.currentTimeMillis() + Duration.ofSeconds(15).toMillis();
		while (System.currentTimeMillis() < deadline) {
			var records = consumer.poll(Duration.ofMillis(500));
			if (!records.isEmpty()) {
				return records.iterator().next();
			}
		}
		throw new AssertionError("No Kafka record received on topic " + topic);
	}
}
