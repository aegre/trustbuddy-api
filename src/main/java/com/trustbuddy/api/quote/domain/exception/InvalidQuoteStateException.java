package com.trustbuddy.api.quote.domain.exception;

import com.trustbuddy.api.quote.domain.model.QuoteStatus;

public class InvalidQuoteStateException extends DomainException {

		public InvalidQuoteStateException(QuoteStatus currentStatus, String operation) {
				super("Cannot " + operation + " while quote is in status " + currentStatus);
		}
}
