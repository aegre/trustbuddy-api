package com.trustbuddy.api.quote.domain.exception;

import com.trustbuddy.api.quote.domain.model.QuoteStatus;

public class InvalidQuoteStateException extends DomainException {

		public InvalidQuoteStateException(QuoteStatus currentStatus, String operation) {
				super(resolveErrorCode(currentStatus), buildMessage(currentStatus, operation));
		}

		public InvalidQuoteStateException(String errorCode, String message) {
				super(errorCode, message);
		}

		private static String resolveErrorCode(QuoteStatus status) {
				if (status == QuoteStatus.EXPIRED) {
						return QuoteErrorCodes.QUOTE_EXPIRED;
				}
				return QuoteErrorCodes.QUOTE_INVALID_STATUS;
		}

		private static String buildMessage(QuoteStatus currentStatus, String operation) {
				return "Cannot " + operation + " while quote is in status " + currentStatus;
		}
}
