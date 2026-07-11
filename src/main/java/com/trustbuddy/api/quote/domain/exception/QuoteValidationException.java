package com.trustbuddy.api.quote.domain.exception;

public class QuoteValidationException extends DomainException {

		public QuoteValidationException(String message) {
				super(QuoteErrorCodes.QUOTE_VALIDATION_FAILED, message);
		}

		public QuoteValidationException(String errorCode, String message) {
				super(errorCode, message);
		}
}
