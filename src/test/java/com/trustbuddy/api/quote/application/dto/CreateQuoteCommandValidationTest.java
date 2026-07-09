package com.trustbuddy.api.quote.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateQuoteCommandValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void givenValidCommand_whenValidate_thenHasNoViolations() {
		// Given
		CreateQuoteCommand command = validCommand();

		// When
		Set<ConstraintViolation<CreateQuoteCommand>> violations = validator.validate(command);

		// Then
		assertThat(violations).isEmpty();
	}

	@Test
	void givenBlankName_whenValidate_thenReportsNameViolation() {
		// Given
		CreateQuoteCommand command = validCommand();
		command.setName("  ");

		// When
		Set<ConstraintViolation<CreateQuoteCommand>> violations = validator.validate(command);

		// Then
		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("name");
	}

	@Test
	void givenInvalidEmail_whenValidate_thenReportsEmailViolation() {
		// Given
		CreateQuoteCommand command = validCommand();
		command.setEmail("not-an-email");

		// When
		Set<ConstraintViolation<CreateQuoteCommand>> violations = validator.validate(command);

		// Then
		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("email");
	}

	@Test
	void givenInvalidZipCode_whenValidate_thenReportsZipCodeViolation() {
		// Given
		CreateQuoteCommand command = validCommand();
		command.setZipCode("ABCDE");

		// When
		Set<ConstraintViolation<CreateQuoteCommand>> violations = validator.validate(command);

		// Then
		assertThat(violations).extracting(violation -> violation.getPropertyPath().toString())
				.contains("zipCode");
	}

	private static CreateQuoteCommand validCommand() {
		CreateQuoteCommand command = new CreateQuoteCommand();
		command.setName("Jane Doe");
		command.setEmail("jane@example.com");
		command.setAge(30);
		command.setZipCode(QuoteFieldConstraints.ZIP_CODE_EXAMPLE);
		return command;
	}
}
