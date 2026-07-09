package com.trustbuddy.api.quote.domain.service;

import java.util.Set;

import com.trustbuddy.api.quote.domain.exception.ConditionalFieldRejectedException;
import com.trustbuddy.api.quote.domain.model.ConditionType;

/**
 * Enforces the challenge rule that supplemental health fields are only accepted
 * when the applicant is older than 65.
 */
public class CoverageHealthPolicy {

	private static final int SENIOR_AGE_THRESHOLD = 65;

	public void validateHealthFieldsForAge(
			int age,
			Boolean hasPreexistingConditions,
			Set<ConditionType> conditions,
			Boolean takesPrescriptionMedication,
			Boolean usesTobacco,
			Boolean needsSpouseCoverage) {
		if (age > SENIOR_AGE_THRESHOLD) {
			return;
		}
		if (hasAnyHealthField(
				hasPreexistingConditions,
				conditions,
				takesPrescriptionMedication,
				usesTobacco,
				needsSpouseCoverage)) {
			throw new ConditionalFieldRejectedException(
					"Supplemental health fields are not allowed when age is 65 or younger");
		}
	}

	private boolean hasAnyHealthField(
			Boolean hasPreexistingConditions,
			Set<ConditionType> conditions,
			Boolean takesPrescriptionMedication,
			Boolean usesTobacco,
			Boolean needsSpouseCoverage) {
		return hasPreexistingConditions != null
				|| conditions != null
				|| takesPrescriptionMedication != null
				|| usesTobacco != null
				|| needsSpouseCoverage != null;
	}
}
