package com.trustbuddy.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.redis.testcontainers.RedisContainer;

@Testcontainers
@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@ActiveProfiles("test")
class TrustbuddyApiApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

	@Container
	@ServiceConnection
	static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

	@Test
	void contextLoads() {
		// Given — full application context with PostgreSQL and Redis (see class annotations)
		// When / Then — context starts without failure
	}

}
