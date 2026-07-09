package com.trustbuddy.api.quote.domain.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trustbuddy.api.quote.domain.exception.ConditionalFieldRejectedException;
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
		assertThatCode(() -> coverageHealthPolicy.validateHealthFieldsForAge(
				age, null, null, null, null, null))
				.doesNotThrowAnyException();
	}

	@Test
	void givenAgeAbove65WithHealthFields_whenValidate_thenPasses() {
		// Given
		int age = 70;

		// When / Then
		assertThatCode(() -> coverageHealthPolicy.validateHealthFieldsForAge(
				age,
				true,
				Set.of(ConditionType.DIABETES),
				false,
				true,
				true))
				.doesNotThrowAnyException();
	}

	@Test
	void givenAgeAtMost65WithTobaccoField_whenValidate_thenThrowsConditionalFieldRejectedException() {
		// Given
		int age = 65;

		// When / Then
		assertThatThrownBy(() -> coverageHealthPolicy.validateHealthFieldsForAge(
				age, null, null, null, false, null))
				.isInstanceOf(ConditionalFieldRejectedException.class)
				.hasMessageContaining("65 or younger");
	}

	@Test
	void givenAgeAtMost65WithEmptyConditions_whenValidate_thenThrowsConditionalFieldRejectedException() {
		// Given
		int age = 40;

		// When / Then
		assertThatThrownBy(() -> coverageHealthPolicy.validateHealthFieldsForAge(
				age, null, Set.of(), null, null, null))
				.isInstanceOf(ConditionalFieldRejectedException.class);
	}
}
