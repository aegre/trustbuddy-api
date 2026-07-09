package com.trustbuddy.api.quote.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;

class PremiumCalculatorTest {

	private PremiumCalculator premiumCalculator;

	@BeforeEach
	void setUp() {
		premiumCalculator = new PremiumCalculator();
	}

	@Test
	void givenSeniorWithConditionTobaccoAndSpouse_whenCalculate_thenReturns327Point60() {
		// Given
		Quote quote = QuoteGenerator.coverage(70, CoverageType.STANDARD)
				.hasPreexistingConditions(true)
				.conditions(ConditionType.DIABETES)
				.usesTobacco(true)
				.needsSpouseCoverage(true)
				.build();

		// When
		BigDecimal premium = premiumCalculator.calculate(quote);

		// Then
		assertThat(premium).isEqualByComparingTo("327.60");
	}

	@Test
	void givenYoungStandardCoverage_whenCalculate_thenReturnsBasePremiumOnly() {
		// Given
		Quote quote = QuoteGenerator.coverage(30, CoverageType.STANDARD).build();

		// When
		BigDecimal premium = premiumCalculator.calculate(quote);

		// Then
		assertThat(premium).isEqualByComparingTo("100.00");
	}

	@Test
	void givenMultipleConditions_whenCalculate_thenAppliesConditionsMultiplierOnce() {
		// Given
		Quote quote = QuoteGenerator.coverage(70, CoverageType.STANDARD)
				.hasPreexistingConditions(true)
				.conditions(
						ConditionType.DIABETES,
						ConditionType.HYPERTENSION,
						ConditionType.HEART_DISEASE)
				.build();

		// When
		BigDecimal premium = premiumCalculator.calculate(quote);

		// Then
		assertThat(premium).isEqualByComparingTo("195.00");
	}

	@Test
	void givenDraftWithoutCoverage_whenCalculate_thenThrowsNullPointerException() {
		// Given
		Quote draft = QuoteGenerator.draft(40);

		// When / Then
		assertThatThrownBy(() -> premiumCalculator.calculate(draft))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("coverageType");
	}
}
