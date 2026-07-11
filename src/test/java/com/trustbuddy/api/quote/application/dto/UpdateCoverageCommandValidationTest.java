package com.trustbuddy.api.quote.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.trustbuddy.api.quote.domain.model.CoverageType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UpdateCoverageCommandValidationTest {

		private Validator validator;

		@BeforeEach
		void setUp() {
				ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
				validator = factory.getValidator();
		}

		@Test
		void givenValidCommand_whenValidate_thenHasNoViolations() {
				// Given
				UpdateCoverageCommand command = new UpdateCoverageCommand();
				command.setCoverageType(CoverageType.STANDARD);

				// When
				Set<ConstraintViolation<UpdateCoverageCommand>> violations = validator.validate(command);

				// Then
				assertThat(violations).isEmpty();
		}

		@Test
		void givenEmptyCommand_whenValidate_thenHasNoViolations() {
				// Given
				UpdateCoverageCommand command = new UpdateCoverageCommand();

				// When
				Set<ConstraintViolation<UpdateCoverageCommand>> violations = validator.validate(command);

				// Then
				assertThat(violations).isEmpty();
		}
}
