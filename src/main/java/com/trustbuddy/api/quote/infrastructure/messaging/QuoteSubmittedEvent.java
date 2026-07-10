package com.trustbuddy.api.quote.infrastructure.messaging;

import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
