package com.trustbuddy.api;

import com.trustbuddy.api.testsupport.PostgresRedisTestcontainers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
				properties =
								"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration")
@ActiveProfiles("test")
class TrustbuddyApiApplicationTests extends PostgresRedisTestcontainers {

		@Test
		void givenApplicationContext_whenStartup_thenContextLoadsSuccessfully() {
				// Given — full application context with PostgreSQL and Redis (see class annotations)
				// When / Then — context starts without failure
		}
}
