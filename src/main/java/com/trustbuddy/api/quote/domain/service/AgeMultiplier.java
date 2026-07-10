package com.trustbuddy.api.quote.domain.service;

import com.trustbuddy.api.quote.domain.model.Quote;
import java.math.BigDecimal;

/** Applies the age factor: {@code × 1.5} when age is greater than 65, otherwise {@code × 1}. */
public class AgeMultiplier implements PremiumMultiplier {

		private static final int SENIOR_AGE_THRESHOLD = 65;
		private static final BigDecimal SENIOR_MULTIPLIER = new BigDecimal("1.5");

		@Override
		public BigDecimal multiplierFor(Quote quote) {
				return quote.getAge() > SENIOR_AGE_THRESHOLD ? SENIOR_MULTIPLIER : BigDecimal.ONE;
		}
}
