package com.trustbuddy.api.quote.domain.service;

import java.math.BigDecimal;

import com.trustbuddy.api.quote.domain.model.Quote;

/**
 * Applies one factor of the monthly premium formula.
 */
public interface PremiumMultiplier {

	BigDecimal multiplierFor(Quote quote);
}
