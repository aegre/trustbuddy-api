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

		public static CoverageDetails initialStandard() {
				return new CoverageDetails(
								CoverageType.STANDARD, null, Set.of(), null, null, null, BigDecimal.ZERO);
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

		public CoverageDetails withoutSeniorOnlyFields() {
				return new CoverageDetails(
								coverageType,
								null,
								Set.of(),
								takesPrescriptionMedication,
								usesTobacco,
								needsSpouseCoverage,
								estimatedMonthlyPremium);
		}
}
