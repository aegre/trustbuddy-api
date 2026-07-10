package com.trustbuddy.api.quote.infrastructure.cache;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/** JSON-serializable snapshot of a {@link Quote} for Redis storage. */
record QuoteCacheDocument(
				UUID id,
				String name,
				String email,
				int age,
				String zipCode,
				CoverageType coverageType,
				Boolean hasPreexistingConditions,
				Set<ConditionType> conditions,
				Boolean takesPrescriptionMedication,
				Boolean usesTobacco,
				Boolean needsSpouseCoverage,
				BigDecimal estimatedMonthlyPremium,
				QuoteStatus status,
				Instant createdAt,
				Instant updatedAt,
				long version) {}
