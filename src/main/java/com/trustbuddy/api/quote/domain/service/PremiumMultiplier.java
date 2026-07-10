package com.trustbuddy.api.quote.domain.service;

import com.trustbuddy.api.quote.domain.model.Quote;
import java.math.BigDecimal;

/** Applies one factor of the monthly premium formula. */
public interface PremiumMultiplier {

		BigDecimal multiplierFor(Quote quote);
}
