package com.trustbuddy.api.quote.domain.exception;

public class ConditionalFieldRejectedException extends DomainException {

		public ConditionalFieldRejectedException(String message) {
				super(QuoteErrorCodes.QUOTE_CONDITIONAL_FIELD_REJECTED, message);
		}
}
