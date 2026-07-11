package com.trustbuddy.api.quote.application.validation;

import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Comparator;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CommandValidator {

		private final Validator validator;

		public CommandValidator(Validator validator) {
				this.validator = validator;
		}

		public <T> void validate(T command) {
				Set<ConstraintViolation<T>> violations = validator.validate(command);
				if (violations.isEmpty()) {
						return;
				}
				throw new QuoteValidationException(formatFirstViolation(violations));
		}

		public <T> void validateSubmissionReadiness(T command) {
				Set<ConstraintViolation<T>> violations = validator.validate(command);
				if (violations.isEmpty()) {
						return;
				}
				throw new InvalidQuoteStateException(formatSubmissionReadinessViolation(violations));
		}

		private <T> String formatSubmissionReadinessViolation(Set<ConstraintViolation<T>> violations) {
				ConstraintViolation<T> first =
								violations.stream()
												.min(
																Comparator.comparing(
																				violation -> violation.getPropertyPath().toString()))
												.orElseThrow();
				return first.getMessage();
		}

		private <T> String formatFirstViolation(Set<ConstraintViolation<T>> violations) {
				ConstraintViolation<T> first =
								violations.stream()
												.min(
																Comparator.comparing(
																				violation -> violation.getPropertyPath().toString()))
												.orElseThrow();
				String propertyPath = first.getPropertyPath().toString();
				return propertyPath.isEmpty()
								? first.getMessage()
								: propertyPath + ": " + first.getMessage();
		}
}
