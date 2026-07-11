package com.trustbuddy.api.quote.domain.exception;

import java.util.UUID;

public class QuoteNotFoundException extends DomainException {

		private final UUID quoteId;

		public QuoteNotFoundException(UUID quoteId) {
				super(QuoteErrorCodes.QUOTE_NOT_FOUND, "Quote not found with id " + quoteId);
				this.quoteId = quoteId;
		}

		public UUID getQuoteId() {
				return quoteId;
		}
}
