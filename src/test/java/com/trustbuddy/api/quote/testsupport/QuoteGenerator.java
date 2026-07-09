package com.trustbuddy.api.quote.testsupport;

import java.math.BigDecimal;
import java.util.Set;

import com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;

/**
 * Builds {@link Quote} instances for tests with sensible defaults.
 * Override only the fields relevant to the scenario under test.
 */
public final class QuoteGenerator {

	private static final String DEFAULT_NAME = "Jane Doe";
	private static final String DEFAULT_EMAIL = "jane@example.com";
	private static final String DEFAULT_ZIP_CODE = QuoteFieldConstraints.ZIP_CODE_EXAMPLE;
	private static final BigDecimal PLACEHOLDER_PREMIUM = BigDecimal.ZERO;

	private QuoteGenerator() {
	}

	public static Quote draft() {
		return draft(30);
	}

	public static Quote draft(int age) {
		return Quote.createDraft(DEFAULT_NAME, DEFAULT_EMAIL, age, DEFAULT_ZIP_CODE);
	}

	public static Quote readyForSubmission(int age) {
		return coverage(age, CoverageType.STANDARD)
				.takesPrescriptionMedication(false)
				.usesTobacco(false)
				.needsSpouseCoverage(false)
				.build();
	}

	public static Quote readyForSubmissionWithoutCoverage(int age) {
		Quote draft = draft(age);
		return Quote.reconstitute(
				draft.getId(),
				draft.getName(),
				draft.getEmail(),
				draft.getAge(),
				draft.getZipCode(),
				null,
				null,
				Set.of(),
				false,
				false,
				false,
				null,
				draft.getStatus(),
				draft.getCreatedAt(),
				draft.getUpdatedAt(),
				draft.getVersion());
	}

	public static Quote readyForSubmissionWithoutTakesPrescriptionMedication(int age) {
		return coverage(age, CoverageType.STANDARD)
				.usesTobacco(false)
				.needsSpouseCoverage(false)
				.build();
	}

	public static CoverageBuilder coverage(int age, CoverageType coverageType) {
		return new CoverageBuilder(age, coverageType);
	}

	public static final class CoverageBuilder {

		private final int age;
		private final CoverageType coverageType;
		private Boolean hasPreexistingConditions;
		private Set<ConditionType> conditions;
		private Boolean takesPrescriptionMedication;
		private Boolean usesTobacco;
		private Boolean needsSpouseCoverage;

		private CoverageBuilder(int age, CoverageType coverageType) {
			this.age = age;
			this.coverageType = coverageType;
		}

		public CoverageBuilder hasPreexistingConditions(boolean hasPreexistingConditions) {
			this.hasPreexistingConditions = hasPreexistingConditions;
			return this;
		}

		public CoverageBuilder conditions(ConditionType... conditions) {
			this.conditions = Set.of(conditions);
			return this;
		}

		public CoverageBuilder conditions(Set<ConditionType> conditions) {
			this.conditions = Set.copyOf(conditions);
			return this;
		}

		public CoverageBuilder takesPrescriptionMedication(boolean takesPrescriptionMedication) {
			this.takesPrescriptionMedication = takesPrescriptionMedication;
			return this;
		}

		public CoverageBuilder usesTobacco(boolean usesTobacco) {
			this.usesTobacco = usesTobacco;
			return this;
		}

		public CoverageBuilder needsSpouseCoverage(boolean needsSpouseCoverage) {
			this.needsSpouseCoverage = needsSpouseCoverage;
			return this;
		}

		public Quote build() {
			return draft(age).applyCoverage(
					coverageType,
					hasPreexistingConditions,
					conditions == null ? Set.of() : conditions,
					takesPrescriptionMedication,
					usesTobacco,
					needsSpouseCoverage,
					PLACEHOLDER_PREMIUM);
		}
	}
}
