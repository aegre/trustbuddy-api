package com.trustbuddy.api.quote.domain.service;

import com.trustbuddy.api.quote.domain.exception.ConditionalFieldRejectedException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import java.util.Set;

/**
 * Validates pre-existing condition fields on coverage updates.
 *
 * <p>These fields are only allowed when age is greater than 65. When allowed, {@code
 * hasPreexistingConditions} is required and {@code conditions} must be present when that answer is
 * {@code true}.
 */
public class CoverageHealthPolicy {

		private static final int SENIOR_AGE_THRESHOLD = 65;

		public void validateHealthFieldsForAge(
						int age, Boolean hasPreexistingConditions, Set<ConditionType> conditions) {
				if (age > SENIOR_AGE_THRESHOLD) {
						validateSeniorPreexistingFields(hasPreexistingConditions, conditions);
				} else {
						rejectSeniorOnlyFields(hasPreexistingConditions, conditions);
				}
		}

		private void validateSeniorPreexistingFields(
						Boolean hasPreexistingConditions, Set<ConditionType> conditions) {
				if (hasPreexistingConditions == null) {
						throw new QuoteValidationException(
										"hasPreexistingConditions is required when age is over 65");
				}
				if (hasPreexistingConditions) {
						if (conditions == null || conditions.isEmpty()) {
								throw new QuoteValidationException(
												"conditions are required when hasPreexistingConditions is true");
						}
						return;
				}
				if (conditions != null && !conditions.isEmpty()) {
						throw new QuoteValidationException(
										"conditions must not be provided when hasPreexistingConditions is false");
				}
		}

		private void rejectSeniorOnlyFields(
						Boolean hasPreexistingConditions, Set<ConditionType> conditions) {
				if (hasPreexistingConditions != null || conditions != null) {
						throw new ConditionalFieldRejectedException(
										"Supplemental health fields are not allowed when age is 65 or younger");
				}
		}
}
