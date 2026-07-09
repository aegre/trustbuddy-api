package com.trustbuddy.api.quote.domain.service;

import java.math.BigDecimal;

import com.trustbuddy.api.quote.domain.model.Quote;

/**
 * Applies the pre-existing conditions factor: {@code × 1.3} when any condition is selected,
 * otherwise {@code × 1}. Multiple conditions still apply the multiplier once.
 */
public class ConditionsMultiplier implements PremiumMultiplier {

	private static final BigDecimal CONDITIONS_MULTIPLIER = new BigDecimal("1.3");

	@Override
	public BigDecimal multiplierFor(Quote quote) {
		return quote.getConditions().isEmpty() ? BigDecimal.ONE : CONDITIONS_MULTIPLIER;
	}
}
