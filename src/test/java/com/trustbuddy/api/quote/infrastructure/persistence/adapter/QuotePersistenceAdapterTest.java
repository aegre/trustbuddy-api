package com.trustbuddy.api.quote.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageDetails;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.PersonalInfo;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteAudit;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.persistence.mapper.QuotePersistenceMapper;
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

@DataJpaTest
@Import({
		QuotePersistenceAdapter.class,
		QuotePersistenceMapper.class,
		QuotePersistenceAdapterTest.CacheTestConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class QuotePersistenceAdapterTest {

		@Container @ServiceConnection
		static PostgreSQLContainer postgres =
						new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

		@Autowired private QuoteRepositoryPort quoteRepository;

		@Test
		void givenDraftQuote_whenSaveAndFindById_thenReturnsPersistedFields() {
				// Given
				Quote draft =
								Quote.createDraft(
												"Jane Doe", "jane@example.com", 30, QuoteFieldConstraints.ZIP_CODE_EXAMPLE);

				// When
				Quote saved = quoteRepository.save(draft);
				Quote found = quoteRepository.findById(saved.getId()).orElseThrow();

				// Then
				assertThat(found.getName()).isEqualTo("Jane Doe");
				assertThat(found.getEmail()).isEqualTo("jane@example.com");
				assertThat(found.getAge()).isEqualTo(30);
				assertThat(found.getZipCode()).isEqualTo(QuoteFieldConstraints.ZIP_CODE_EXAMPLE);
				assertThat(found.getStatus()).isEqualTo(QuoteStatus.DRAFT);
		}

		@Test
		void givenQuoteWithCoverage_whenSave_thenPersistsCoverageTypeAndPremium() {
				// Given
				Quote draft =
								quoteRepository.save(Quote.createDraft("John", "john@example.com", 70, "90210"));
				Quote withCoverage =
								draft.applyCoverage(
												new CoverageDetails(
																CoverageType.STANDARD,
																true,
																Set.of(),
																false,
																false,
																false,
																new BigDecimal("327.60")));

				// When
				Quote saved = quoteRepository.save(withCoverage);
				Quote found = quoteRepository.findById(saved.getId()).orElseThrow();

				// Then
				assertThat(found.getCoverageType()).isEqualTo(CoverageType.STANDARD);
				assertThat(found.getEstimatedMonthlyPremium()).isEqualByComparingTo("327.60");
		}

		@Test
		void givenQuoteWithConditions_whenSave_thenPersistsConditionsAndTobaccoFlag() {
				// Given
				Quote draft =
								quoteRepository.save(Quote.createDraft("John", "john@example.com", 70, "90210"));
				Quote withCoverage =
								draft.applyCoverage(
												new CoverageDetails(
																CoverageType.STANDARD,
																true,
																Set.of(ConditionType.DIABETES, ConditionType.HYPERTENSION),
																true,
																true,
																true,
																new BigDecimal("327.60")));

				// When
				Quote saved = quoteRepository.save(withCoverage);
				Quote found = quoteRepository.findById(saved.getId()).orElseThrow();

				// Then
				assertThat(found.getConditions())
								.containsExactlyInAnyOrder(ConditionType.DIABETES, ConditionType.HYPERTENSION);
				assertThat(found.getUsesTobacco()).isTrue();
		}

		@Test
		void givenTwoSavedQuotes_whenFindAll_thenReturnsPagedResults() {
				// Given
				quoteRepository.save(Quote.createDraft("A", "a@example.com", 25, "11111"));
				quoteRepository.save(Quote.createDraft("B", "b@example.com", 35, "22222"));

				// When
				var page = quoteRepository.findAll(PageRequest.of(0, 10));

				// Then
				assertThat(page.getTotalElements()).isEqualTo(2);
		}

		@Test
		void givenStaleAndFreshDrafts_whenFindStaleDrafts_thenReturnsOnlyStaleDraft() {
				// Given
				Instant staleUpdatedAt = Instant.now().minus(Duration.ofMinutes(31));
				Quote staleDraft =
								Quote.reconstitute(
												java.util.UUID.randomUUID(),
												new PersonalInfo("Stale", "stale@example.com", 40, "33333"),
												null,
												new QuoteAudit(QuoteStatus.DRAFT, staleUpdatedAt, staleUpdatedAt, 0L));
				quoteRepository.save(staleDraft);
				quoteRepository.save(Quote.createDraft("Fresh", "fresh@example.com", 40, "44444"));

				// When
				var staleDrafts =
								quoteRepository.findStaleDrafts(Instant.now().minus(Duration.ofMinutes(30)));

				// Then
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
