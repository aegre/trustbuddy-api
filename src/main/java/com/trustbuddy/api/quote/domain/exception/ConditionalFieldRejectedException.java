package com.trustbuddy.api.quote.domain.exception;

public class ConditionalFieldRejectedException extends RuntimeException {

	public ConditionalFieldRejectedException(String message) {
		super(message);
	}
}
