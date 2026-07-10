package com.trustbuddy.api.quote.infrastructure.cache;

import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.domain.model.Quote;
import java.util.Optional;
import java.util.UUID;

public class NoOpQuoteCacheAdapter implements QuoteCachePort {

		@Override
		public Optional<Quote> get(UUID id) {
				return Optional.empty();
		}

		@Override
		public void put(Quote quote) {
				// no-op
		}

		@Override
		public void evict(UUID id) {
				// no-op
		}
}
