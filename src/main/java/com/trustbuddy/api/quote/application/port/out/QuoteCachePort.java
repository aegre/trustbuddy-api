package com.trustbuddy.api.quote.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.trustbuddy.api.quote.domain.model.Quote;

public interface QuoteCachePort {

	Optional<Quote> get(UUID id);

	void put(Quote quote);

	void evict(UUID id);
}
