package com.trustbuddy.api.quote.domain.service;

import java.math.BigDecimal;

import com.trustbuddy.api.quote.domain.model.Quote;

/**
 * Applies the spouse coverage factor: {@code × 1.4} when spouse coverage is yes, otherwise {@code × 1}.
 */
public class SpouseMultiplier implements PremiumMultiplier {

	private static final BigDecimal SPOUSE_MULTIPLIER = new BigDecimal("1.4");

	@Override
	public BigDecimal multiplierFor(Quote quote) {
		return Boolean.TRUE.equals(quote.getNeedsSpouseCoverage()) ? SPOUSE_MULTIPLIER : BigDecimal.ONE;
	}
}
