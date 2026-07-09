package com.trustbuddy.api.quote.application.validation;

import java.util.Comparator;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

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
		ConstraintViolation<T> first = violations.stream()
				.min(Comparator.comparing(violation -> violation.getPropertyPath().toString()))
				.orElseThrow();
		String propertyPath = first.getPropertyPath().toString();
		String message = propertyPath.isEmpty()
				? first.getMessage()
				: propertyPath + ": " + first.getMessage();
		throw new QuoteValidationException(message);
	}
}
