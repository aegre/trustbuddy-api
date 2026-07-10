package com.trustbuddy.api.quote.application.port.out;

import com.trustbuddy.api.quote.domain.model.Quote;
import java.util.Optional;
import java.util.UUID;

public interface QuoteCachePort {

		Optional<Quote> get(UUID id);

		void put(Quote quote);

		void evict(UUID id);
}
