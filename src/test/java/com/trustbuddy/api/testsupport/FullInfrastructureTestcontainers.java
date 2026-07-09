package com.trustbuddy.api.testsupport;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class FullInfrastructureTestcontainers extends PostgresRedisTestcontainers {

	@Container
	@ServiceConnection
	protected static final KafkaContainer KAFKA =
			new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.0"));
}
