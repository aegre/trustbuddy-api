package com.trustbuddy.api.quote.infrastructure.messaging;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;

public record QuoteSubmittedEvent(
		UUID quoteId,
		QuoteStatus status,
		CoverageType coverageType,
		BigDecimal premium,
		Instant timestamp) {

	public static QuoteSubmittedEvent from(Quote quote) {
		return new QuoteSubmittedEvent(
				quote.getId(),
				quote.getStatus(),
				quote.getCoverageType(),
				quote.getEstimatedMonthlyPremium(),
				Instant.now());
	}
}
