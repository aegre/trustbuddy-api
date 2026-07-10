package com.trustbuddy.api.quote.domain.service;

import com.trustbuddy.api.quote.domain.model.Quote;
import java.math.BigDecimal;

/** Applies the tobacco factor: {@code × 1.2} when tobacco use is yes, otherwise {@code × 1}. */
public class TobaccoMultiplier implements PremiumMultiplier {

		private static final BigDecimal TOBACCO_MULTIPLIER = new BigDecimal("1.2");

		@Override
		public BigDecimal multiplierFor(Quote quote) {
				return Boolean.TRUE.equals(quote.getUsesTobacco()) ? TOBACCO_MULTIPLIER : BigDecimal.ONE;
		}
}
