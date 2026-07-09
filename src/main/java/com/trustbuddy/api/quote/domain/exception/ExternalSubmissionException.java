package com.trustbuddy.api.quote.domain.exception;

public class ExternalSubmissionException extends DomainException {

	public ExternalSubmissionException(String message) {
		super(message);
	}

	public ExternalSubmissionException(String message, Throwable cause) {
		super(message, cause);
	}
}
