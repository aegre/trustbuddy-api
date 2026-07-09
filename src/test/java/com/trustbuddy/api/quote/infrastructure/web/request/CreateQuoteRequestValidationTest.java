package com.trustbuddy.api.quote.infrastructure.web.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateQuoteRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void givenValidRequest_whenValidate_thenHasNoViolations() {
		// Given
		CreateQuoteRequest request = validRequest();

		// When
		Set<ConstraintViolation<CreateQuoteRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void givenBlankName_whenValidate_thenReportsNameViolation() {
		// Given
		CreateQuoteRequest request = validRequest();
		request.setName("  ");

		// When
		Set<ConstraintViolation<CreateQuoteRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("name");
	}

	@Test
	void givenInvalidEmail_whenValidate_thenReportsEmailViolation() {
		// Given
		CreateQuoteRequest request = validRequest();
		request.setEmail("not-an-email");

		// When
		Set<ConstraintViolation<CreateQuoteRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("email");
	}

	@Test
	void givenInvalidZipCode_whenValidate_thenReportsZipCodeViolation() {
		// Given
		CreateQuoteRequest request = validRequest();
		request.setZipCode("ABCDE");

		// When
		Set<ConstraintViolation<CreateQuoteRequest>> violations = validator.validate(request);

		// Then
		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("zipCode");
	}

	private static CreateQuoteRequest validRequest() {
		CreateQuoteRequest request = new CreateQuoteRequest();
		request.setName("Jane Doe");
		request.setEmail("jane@example.com");
		request.setAge(30);
		request.setZipCode("12345");
		return request;
	}
}
