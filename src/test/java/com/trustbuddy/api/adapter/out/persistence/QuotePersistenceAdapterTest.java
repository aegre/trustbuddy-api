package com.trustbuddy.api.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.trustbuddy.api.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.domain.model.ConditionType;
import com.trustbuddy.api.domain.model.CoverageType;
import com.trustbuddy.api.domain.model.Quote;
import com.trustbuddy.api.domain.model.QuoteStatus;

@DataJpaTest
@Import({ QuotePersistenceAdapter.class, QuotePersistenceAdapterTest.CacheTestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class QuotePersistenceAdapterTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

	@Autowired
	private QuoteRepositoryPort quoteRepository;

	@Test
	void saveAndFindById_roundTripsQuoteFields() {
		Quote draft = Quote.createDraft("Jane Doe", "jane@example.com", 30, "12345");

		Quote saved = quoteRepository.save(draft);

		Quote found = quoteRepository.findById(saved.getId()).orElseThrow();
		assertThat(found.getName()).isEqualTo("Jane Doe");
		assertThat(found.getEmail()).isEqualTo("jane@example.com");
		assertThat(found.getAge()).isEqualTo(30);
		assertThat(found.getZipCode()).isEqualTo("12345");
		assertThat(found.getStatus()).isEqualTo(QuoteStatus.DRAFT);
	}

	@Test
	void save_persistsCoverageAndConditions() {
		Quote draft = quoteRepository.save(Quote.createDraft("John", "john@example.com", 70, "90210"));
		Quote withCoverage = draft.applyCoverage(
				CoverageType.STANDARD,
				true,
				Set.of(ConditionType.DIABETES, ConditionType.HYPERTENSION),
				true,
				true,
				true,
				new BigDecimal("327.60"));

		Quote saved = quoteRepository.save(withCoverage);

		Quote found = quoteRepository.findById(saved.getId()).orElseThrow();
		assertThat(found.getCoverageType()).isEqualTo(CoverageType.STANDARD);
		assertThat(found.getEstimatedMonthlyPremium()).isEqualByComparingTo("327.60");
		assertThat(found.getConditions()).containsExactlyInAnyOrder(
				ConditionType.DIABETES,
				ConditionType.HYPERTENSION);
		assertThat(found.getUsesTobacco()).isTrue();
	}

	@Test
	void findAll_returnsPagedQuotes() {
		quoteRepository.save(Quote.createDraft("A", "a@example.com", 25, "11111"));
		quoteRepository.save(Quote.createDraft("B", "b@example.com", 35, "22222"));

		var page = quoteRepository.findAll(PageRequest.of(0, 10));

		assertThat(page.getTotalElements()).isEqualTo(2);
	}

	@Test
	void findStaleDrafts_returnsDraftsOlderThanCutoff() {
		Instant staleUpdatedAt = Instant.now().minus(Duration.ofMinutes(31));
		Quote staleDraft = Quote.reconstitute(
				java.util.UUID.randomUUID(),
				"Stale",
				"stale@example.com",
				40,
				"33333",
				null,
				null,
				Set.of(),
				null,
				null,
				null,
				null,
				QuoteStatus.DRAFT,
				staleUpdatedAt,
				staleUpdatedAt,
				0L);
		quoteRepository.save(staleDraft);
		quoteRepository.save(Quote.createDraft("Fresh", "fresh@example.com", 40, "44444"));

		var staleDrafts = quoteRepository.findStaleDrafts(Instant.now().minus(Duration.ofMinutes(30)));

		assertThat(staleDrafts).extracting(Quote::getEmail).containsExactly("stale@example.com");
	}

	@TestConfiguration
	static class CacheTestConfig {

		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager();
		}
	}
}
