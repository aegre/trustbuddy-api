package com.trustbuddy.api.quote.infrastructure.web.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.trustbuddy.api.quote.domain.model.CoverageType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UpdateCoverageRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void givenCoverageTypeOnly_whenValidate_thenHasNoViolations() {
		// Given
		UpdateCoverageRequest request = new UpdateCoverageRequest();
		request.setCoverageType(CoverageType.STANDARD);

		// When
		Set<ConstraintViolation<UpdateCoverageRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void givenMissingCoverageType_whenValidate_thenReportsCoverageTypeViolation() {
		// Given
		UpdateCoverageRequest request = new UpdateCoverageRequest();

		// When
		Set<ConstraintViolation<UpdateCoverageRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("coverageType");
	}
}
