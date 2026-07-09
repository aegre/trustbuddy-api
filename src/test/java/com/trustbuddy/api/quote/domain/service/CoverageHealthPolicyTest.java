package com.trustbuddy.api.quote.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trustbuddy.api.quote.domain.exception.ConditionalFieldRejectedException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.ConditionType;

class CoverageHealthPolicyTest {

	private CoverageHealthPolicy coverageHealthPolicy;

	@BeforeEach
	void setUp() {
		coverageHealthPolicy = new CoverageHealthPolicy();
	}

	@Test
	void givenAgeAtMost65WithNoHealthFields_whenValidate_thenPasses() {
		// Given
		int age = 30;

		// When / Then
		assertThatCode(() -> coverageHealthPolicy.validateHealthFieldsForAge(age, null, null))
				.doesNotThrowAnyException();
	}

	@Test
	void givenAgeAbove65WithCompleteHealthFields_whenValidate_thenPasses() {
		// Given
		int age = 70;

		// When / Then
		assertThatCode(() -> coverageHealthPolicy.validateHealthFieldsForAge(
				age, true, Set.of(ConditionType.DIABETES)))
				.doesNotThrowAnyException();
	}

	@Test
	void givenAgeAbove65WithNoPreexistingConditions_whenValidate_thenPasses() {
		// Given
		int age = 70;

		// When / Then
		assertThatCode(() -> coverageHealthPolicy.validateHealthFieldsForAge(age, false, null))
				.doesNotThrowAnyException();
	}

	@Test
	void givenAgeAbove65WithoutHasPreexistingConditions_whenValidate_thenThrowsQuoteValidationException() {
		// Given
		int age = 70;

		// When / Then
		assertThatThrownBy(() -> coverageHealthPolicy.validateHealthFieldsForAge(age, null, null))
				.isInstanceOf(QuoteValidationException.class)
				.hasMessageContaining("hasPreexistingConditions is required");
	}

	@Test
	void givenAgeAbove65WithPreexistingTrueAndNoConditions_whenValidate_thenThrowsQuoteValidationException() {
		// Given
		int age = 70;

		// When / Then
		assertThatThrownBy(() -> coverageHealthPolicy.validateHealthFieldsForAge(age, true, null))
				.isInstanceOf(QuoteValidationException.class)
				.hasMessageContaining("conditions are required");
	}

	@Test
	void givenAgeAbove65WithPreexistingFalseAndConditions_whenValidate_thenThrowsQuoteValidationException() {
		// Given
		int age = 70;

		// When / Then
		assertThatThrownBy(() -> coverageHealthPolicy.validateHealthFieldsForAge(
				age, false, Set.of(ConditionType.DIABETES)))
				.isInstanceOf(QuoteValidationException.class)
				.hasMessageContaining("conditions must not be provided");
	}

	@Test
	void givenAgeAtMost65WithPreexistingField_whenValidate_thenThrowsConditionalFieldRejectedException() {
		// Given
		int age = 40;

		// When / Then
		assertThatThrownBy(() -> coverageHealthPolicy.validateHealthFieldsForAge(age, true, null))
				.isInstanceOf(ConditionalFieldRejectedException.class)
				.hasMessageContaining("65 or younger");
	}

	@Test
	void givenAgeAtMost65WithConditionsField_whenValidate_thenThrowsConditionalFieldRejectedException() {
		// Given
		int age = 40;

		// When / Then
		assertThatThrownBy(() -> coverageHealthPolicy.validateHealthFieldsForAge(age, null, Set.of()))
				.isInstanceOf(ConditionalFieldRejectedException.class);
	}
}
