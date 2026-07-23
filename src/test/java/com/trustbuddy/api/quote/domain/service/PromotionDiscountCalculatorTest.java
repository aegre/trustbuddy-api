package com.trustbuddy.api.quote.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PromotionDiscountCalculatorTest {

		private PromotionDiscountCalculator calculator;

		@BeforeEach
		void setUp() {
				calculator = new PromotionDiscountCalculator();
		}

		@Test
		void givenPremium100AndTenPercent_whenCalculate_thenReturns10Point00() {
				// Given
				BigDecimal premium = new BigDecimal("100.00");
				BigDecimal percentage = new BigDecimal("10");

				// When
				BigDecimal discount = calculator.calculate(premium, percentage);

				// Then
				assertThat(discount).isEqualByComparingTo("10.00");
		}

		@Test
		void givenPremium327Point60AndFifteenPercent_whenCalculate_thenReturns49Point14() {
				// Given
				BigDecimal premium = new BigDecimal("327.60");
				BigDecimal percentage = new BigDecimal("15");

				// When
				BigDecimal discount = calculator.calculate(premium, percentage);

				// Then
				assertThat(discount).isEqualByComparingTo("49.14");
		}

		@Test
		void givenPremiumThatNeedsRounding_whenCalculate_thenRoundsHalfUpToTwoDecimals() {
				// Given
				BigDecimal premium = new BigDecimal("100.00");
				BigDecimal percentage = new BigDecimal("10.005");

				// When
				BigDecimal discount = calculator.calculate(premium, percentage);

				// Then
				assertThat(discount).isEqualByComparingTo("10.01");
		}

		@Test
		void givenZeroPremium_whenCalculate_thenReturnsZero() {
				// Given
				BigDecimal premium = BigDecimal.ZERO;
				BigDecimal percentage = new BigDecimal("25");

				// When
				BigDecimal discount = calculator.calculate(premium, percentage);

				// Then
				assertThat(discount).isEqualByComparingTo("0.00");
		}
}
