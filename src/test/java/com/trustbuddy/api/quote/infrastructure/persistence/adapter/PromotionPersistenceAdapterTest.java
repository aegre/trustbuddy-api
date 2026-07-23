package com.trustbuddy.api.quote.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.trustbuddy.api.quote.application.port.out.PromotionRepositoryPort;
import com.trustbuddy.api.quote.domain.model.Promotion;
import com.trustbuddy.api.quote.infrastructure.persistence.entity.PromotionEntity;
import com.trustbuddy.api.quote.infrastructure.persistence.mapper.PromotionPersistenceMapper;
import com.trustbuddy.api.quote.infrastructure.persistence.repository.PromotionJpaRepository;
import com.trustbuddy.api.quote.testsupport.PromotionGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
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
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@Import({
		PromotionPersistenceAdapter.class,
		PromotionPersistenceMapper.class,
		PromotionPersistenceAdapterTest.CacheTestConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class PromotionPersistenceAdapterTest {

		@Container @ServiceConnection
		static PostgreSQLContainer postgres =
						new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

		@Autowired private PromotionRepositoryPort promotionRepository;

		@Autowired private PromotionJpaRepository jpaRepository;

		@Test
		void givenPersistedPromotion_whenFindByCode_thenReturnsDomainPromotion() {
				// Given
				UUID id = UUID.randomUUID();
				Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
				Instant endsAt = Instant.parse("2026-12-31T23:59:59Z");
				persistPromotion(id, "SAVE10", new BigDecimal("10.00"), true, startsAt, endsAt);

				// When
				Optional<Promotion> found = promotionRepository.findByCode("SAVE10");

				// Then
				assertThat(found).isPresent();
				Promotion promotion = found.orElseThrow();
				assertThat(promotion.id()).isEqualTo(id);
				assertThat(promotion.code()).isEqualTo("SAVE10");
				assertThat(promotion.percentage()).isEqualByComparingTo("10.00");
				assertThat(promotion.active()).isTrue();
				assertThat(promotion.startsAt()).isEqualTo(startsAt);
				assertThat(promotion.endsAt()).isEqualTo(endsAt);
		}

		@Test
		void givenPersistedPromotion_whenFindByCodeWithDifferentCase_thenReturnsPromotion() {
				// Given
				Promotion seed = PromotionGenerator.active("WELCOME15", new BigDecimal("15"));
				persistPromotion(
								seed.id(),
								seed.code(),
								seed.percentage(),
								seed.active(),
								seed.startsAt(),
								seed.endsAt());

				// When
				Optional<Promotion> found = promotionRepository.findByCode("  welcome15  ");

				// Then
				assertThat(found).isPresent();
				assertThat(found.orElseThrow().code()).isEqualTo("WELCOME15");
		}

		@Test
		void givenNoMatchingPromotion_whenFindByCode_thenReturnsEmpty() {
				// Given / When
				Optional<Promotion> found = promotionRepository.findByCode("MISSING");

				// Then
				assertThat(found).isEmpty();
		}

		private void persistPromotion(
						UUID id,
						String code,
						BigDecimal percentage,
						boolean active,
						Instant startsAt,
						Instant endsAt) {
				PromotionEntity entity = new PromotionEntity();
				entity.setId(id);
				entity.setCode(code);
				entity.setPercentage(percentage);
				entity.setActive(active);
				entity.setStartsAt(startsAt);
				entity.setEndsAt(endsAt);
				jpaRepository.save(entity);
		}

		@TestConfiguration
		static class CacheTestConfig {

				@Bean
				CacheManager cacheManager() {
						return new ConcurrentMapCacheManager();
				}
		}
}
