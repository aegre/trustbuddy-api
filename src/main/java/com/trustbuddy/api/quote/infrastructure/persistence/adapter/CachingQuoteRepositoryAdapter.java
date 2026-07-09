package com.trustbuddy.api.quote.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.model.Quote;

@Component
@Primary
public class CachingQuoteRepositoryAdapter implements QuoteRepositoryPort {

	private final QuotePersistenceAdapter delegate;
	private final ObjectProvider<QuoteCachePort> quoteCache;

	public CachingQuoteRepositoryAdapter(
			QuotePersistenceAdapter delegate,
			ObjectProvider<QuoteCachePort> quoteCache) {
		this.delegate = delegate;
		this.quoteCache = quoteCache;
	}

	@Override
	public Quote save(Quote quote) {
		Quote saved = delegate.save(quote);
		quoteCache.getObject().evict(saved.getId());
		return saved;
	}

	@Override
	public Optional<Quote> findById(UUID id) {
		return delegate.findById(id);
	}

	@Override
	public Page<Quote> findAll(Pageable pageable) {
		return delegate.findAll(pageable);
	}

	@Override
	public List<Quote> findStaleDrafts(Instant updatedBefore) {
		return delegate.findStaleDrafts(updatedBefore);
	}
}
