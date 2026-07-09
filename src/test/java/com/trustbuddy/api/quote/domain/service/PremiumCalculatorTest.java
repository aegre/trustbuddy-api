package com.trustbuddy.api.quote.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;

class PremiumCalculatorTest {

	private PremiumCalculator premiumCalculator;

	@BeforeEach
	void setUp() {
		premiumCalculator = new PremiumCalculator();
	}

	@Test
	void givenSeniorWithConditionTobaccoAndSpouse_whenCalculate_thenReturns327Point60() {
		// Given
		Quote quote = Quote.createDraft("Jane", "jane@example.com", 70, "12345")
				.applyCoverage(
						CoverageType.STANDARD,
						true,
						Set.of(ConditionType.DIABETES),
						false,
						true,
						true,
						BigDecimal.ZERO);

		// When
		BigDecimal premium = premiumCalculator.calculate(quote);

		// Then
		assertThat(premium).isEqualByComparingTo("327.60");
	}

	@Test
	void givenYoungStandardCoverage_whenCalculate_thenReturnsBasePremiumOnly() {
		// Given
		Quote quote = Quote.createDraft("Alex", "alex@example.com", 30, "12345")
				.applyCoverage(
						CoverageType.STANDARD,
						false,
						Set.of(),
						false,
						false,
						false,
						BigDecimal.ZERO);

		// When
		BigDecimal premium = premiumCalculator.calculate(quote);

		// Then
		assertThat(premium).isEqualByComparingTo("100.00");
	}

	@Test
	void givenMultipleConditions_whenCalculate_thenAppliesConditionsMultiplierOnce() {
		// Given
		Quote quote = Quote.createDraft("John", "john@example.com", 70, "90210")
				.applyCoverage(
						CoverageType.STANDARD,
						true,
						Set.of(
								ConditionType.DIABETES,
								ConditionType.HYPERTENSION,
								ConditionType.HEART_DISEASE),
						false,
						false,
						false,
						BigDecimal.ZERO);

		// When
		BigDecimal premium = premiumCalculator.calculate(quote);

		// Then
		assertThat(premium).isEqualByComparingTo("195.00");
	}

	@Test
	void givenDraftWithoutCoverage_whenCalculate_thenThrowsNullPointerException() {
		// Given
		Quote draft = Quote.createDraft("No", "coverage@example.com", 40, "11111");

		// When / Then
		assertThatThrownBy(() -> premiumCalculator.calculate(draft))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("coverageType");
	}
}
