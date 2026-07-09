package com.trustbuddy.api.quote.application.port.out;

import com.trustbuddy.api.quote.domain.model.Quote;

public interface QuoteEventPublisherPort {

	void publishQuoteSubmitted(Quote quote);
}
