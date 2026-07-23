package com.trustbuddy.api.quote.domain.exception;

public final class QuoteErrorCodes {

		public static final String QUOTE_NOT_FOUND = "QUOTE_NOT_FOUND";
		public static final String QUOTE_EXPIRED = "QUOTE_EXPIRED";
		public static final String QUOTE_INVALID_STATUS = "QUOTE_INVALID_STATUS";
		public static final String QUOTE_NOT_READY = "QUOTE_NOT_READY";
		public static final String QUOTE_MISSING_COVERAGE = "QUOTE_MISSING_COVERAGE";
		public static final String QUOTE_MISSING_HEALTH_FIELDS = "QUOTE_MISSING_HEALTH_FIELDS";
		public static final String QUOTE_VALIDATION_FAILED = "QUOTE_VALIDATION_FAILED";
		public static final String QUOTE_INVALID_QUERY = "QUOTE_INVALID_QUERY";
		public static final String QUOTE_CONDITIONAL_FIELD_REJECTED =
						"QUOTE_CONDITIONAL_FIELD_REJECTED";
		public static final String QUOTE_EXTERNAL_SUBMISSION_FAILED =
						"QUOTE_EXTERNAL_SUBMISSION_FAILED";
		public static final String PROMO_NOT_FOUND = "PROMO_NOT_FOUND";
		public static final String PROMO_INVALID = "PROMO_INVALID";
		public static final String PROMO_REQUIRES_PREMIUM = "PROMO_REQUIRES_PREMIUM";

		private QuoteErrorCodes() {}
}
