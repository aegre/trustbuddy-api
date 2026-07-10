package com.trustbuddy.api.quote.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

public record CoverageDetails(
				CoverageType coverageType,
				Boolean hasPreexistingConditions,
				Set<ConditionType> conditions,
				Boolean takesPrescriptionMedication,
				Boolean usesTobacco,
				Boolean needsSpouseCoverage,
				BigDecimal estimatedMonthlyPremium) {

		public CoverageDetails {
				Objects.requireNonNull(coverageType, "coverageType");
				conditions = conditions == null ? Set.of() : Set.copyOf(conditions);
		}

		public CoverageDetails withPremium(BigDecimal premium) {
				return new CoverageDetails(
								coverageType,
								hasPreexistingConditions,
								conditions,
								takesPrescriptionMedication,
								usesTobacco,
								needsSpouseCoverage,
								Objects.requireNonNull(premium, "estimatedMonthlyPremium"));
		}
}
