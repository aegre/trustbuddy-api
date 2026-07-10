package com.trustbuddy.api.quote.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.redis.testcontainers.RedisContainer;
import com.trustbuddy.api.config.CacheConfig;
import com.trustbuddy.api.config.properties.QuoteProperties;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = RedisQuoteCacheAdapterIT.TestApplication.class)
@Testcontainers
@ActiveProfiles("test")
class RedisQuoteCacheAdapterIT {

		@Configuration
		@Import({
				DataRedisAutoConfiguration.class,
				CacheConfig.class,
				RedisQuoteCacheConfiguration.class,
				QuoteCacheMapper.class
		})
		@EnableConfigurationProperties(QuoteProperties.class)
		static class TestApplication {}

		@Container @ServiceConnection
		static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7-alpine"));

		@Autowired private QuoteCachePort quoteCache;

		@Test
		void givenQuote_whenPutGetEvict_thenRoundTripsThroughRedis() {
				// Given
				Quote quote =
								QuoteGenerator.coverage(30, CoverageType.STANDARD)
												.takesPrescriptionMedication(false)
												.usesTobacco(false)
												.needsSpouseCoverage(false)
												.build();

				// When
				quoteCache.put(quote);
				var cached = quoteCache.get(quote.getId());
				quoteCache.evict(quote.getId());
				var afterEvict = quoteCache.get(quote.getId());

				// Then
				assertThat(cached).get().usingRecursiveComparison().isEqualTo(quote);
				assertThat(afterEvict).isEmpty();
		}
}
