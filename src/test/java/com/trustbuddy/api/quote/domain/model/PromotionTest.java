package com.trustbuddy.api.quote.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trustbuddy.api.quote.testsupport.PromotionGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class PromotionTest {

		@Test
		void givenLowercaseCodeWithSpaces_whenNormalizeCode_thenReturnsTrimmedUppercase() {
				// Given
				String rawCode = "  save10  ";

				// When
				String normalized = Promotion.normalizeCode(rawCode);

				// Then
				assertThat(normalized).isEqualTo("SAVE10");
		}

		@Test
		void givenLowercaseCode_whenCreatePromotion_thenStoresNormalizedCode() {
				// Given / When
				Promotion promotion = PromotionGenerator.active("save10", new BigDecimal("15"));

				// Then
				assertThat(promotion.code()).isEqualTo("SAVE10");
		}

		@Test
		void givenActivePromotionWithinWindow_whenIsApplicableAt_thenReturnsTrue() {
				// Given
				Instant now = Instant.parse("2026-06-15T12:00:00Z");
				Promotion promotion =
								PromotionGenerator.builder()
												.active(true)
												.startsAt(Instant.parse("2026-06-01T00:00:00Z"))
												.endsAt(Instant.parse("2026-06-30T23:59:59Z"))
												.build();

				// When
				boolean applicable = promotion.isApplicableAt(now);

				// Then
				assertThat(applicable).isTrue();
		}

		@Test
		void givenInactivePromotion_whenIsApplicableAt_thenReturnsFalse() {
				// Given
				Promotion promotion = PromotionGenerator.builder().active(false).build();

				// When
				boolean applicable = promotion.isApplicableAt(Instant.now());

				// Then
				assertThat(applicable).isFalse();
		}

		@Test
		void givenPromotionBeforeStart_whenIsApplicableAt_thenReturnsFalse() {
				// Given
				Instant now = Instant.parse("2026-05-31T23:59:59Z");
				Promotion promotion =
								PromotionGenerator.builder()
												.startsAt(Instant.parse("2026-06-01T00:00:00Z"))
												.endsAt(Instant.parse("2026-06-30T23:59:59Z"))
												.build();

				// When
				boolean applicable = promotion.isApplicableAt(now);

				// Then
				assertThat(applicable).isFalse();
		}

		@Test
		void givenPromotionAfterEnd_whenIsApplicableAt_thenReturnsFalse() {
				// Given
				Instant now = Instant.parse("2026-07-01T00:00:00Z");
				Promotion promotion =
								PromotionGenerator.builder()
												.startsAt(Instant.parse("2026-06-01T00:00:00Z"))
												.endsAt(Instant.parse("2026-06-30T23:59:59Z"))
												.build();

				// When
				boolean applicable = promotion.isApplicableAt(now);

				// Then
				assertThat(applicable).isFalse();
		}

		@Test
		void givenPromotionAtWindowBoundaries_whenIsApplicableAt_thenReturnsTrue() {
				// Given
				Instant startsAt = Instant.parse("2026-06-01T00:00:00Z");
				Instant endsAt = Instant.parse("2026-06-30T23:59:59Z");
				Promotion promotion =
								PromotionGenerator.builder().startsAt(startsAt).endsAt(endsAt).build();

				// When / Then
				assertThat(promotion.isApplicableAt(startsAt)).isTrue();
				assertThat(promotion.isApplicableAt(endsAt)).isTrue();
		}

		@Test
		void givenZeroPercentage_whenCreatePromotion_thenThrowsIllegalArgumentException() {
				// Given / When / Then
				assertThatThrownBy(() -> PromotionGenerator.builder().percentage(BigDecimal.ZERO).build())
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("percentage");
		}

		@Test
		void givenEndsAtBeforeStartsAt_whenCreatePromotion_thenThrowsIllegalArgumentException() {
				// Given
				Instant startsAt = Instant.now();
				Instant endsAt = startsAt.minus(1, ChronoUnit.DAYS);

				// When / Then
				assertThatThrownBy(
												() ->
																PromotionGenerator.builder()
																				.startsAt(startsAt)
																				.endsAt(endsAt)
																				.build())
								.isInstanceOf(IllegalArgumentException.class)
								.hasMessageContaining("endsAt");
		}
}
