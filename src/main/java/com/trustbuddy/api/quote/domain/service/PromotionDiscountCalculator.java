package com.trustbuddy.api.quote.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calculates a promotional discount as a percentage of premium.
 *
 * <p>Formula: {@code premium × percentage / 100}, rounded {@link RoundingMode#HALF_UP} to two
 * decimal places.
 */
public class PromotionDiscountCalculator {

		private static final int DISCOUNT_SCALE = 2;
		private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

		public BigDecimal calculate(BigDecimal premium, BigDecimal percentage) {
				Objects.requireNonNull(premium, "premium");
				Objects.requireNonNull(percentage, "percentage");
				return premium.multiply(percentage)
								.divide(ONE_HUNDRED, DISCOUNT_SCALE, RoundingMode.HALF_UP);
		}
}
