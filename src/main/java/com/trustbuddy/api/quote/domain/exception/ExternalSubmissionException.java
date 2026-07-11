package com.trustbuddy.api.quote.domain.exception;

public class ExternalSubmissionException extends DomainException {

		public ExternalSubmissionException(String message) {
				super(QuoteErrorCodes.QUOTE_EXTERNAL_SUBMISSION_FAILED, message);
		}

		public ExternalSubmissionException(String message, Throwable cause) {
				super(QuoteErrorCodes.QUOTE_EXTERNAL_SUBMISSION_FAILED, message, cause);
		}
}
