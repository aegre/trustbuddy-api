package com.trustbuddy.api.quote.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trustbuddy.api.config.properties.QuoteProperties;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;

@Service
public class DraftExpirationService {

	private final QuoteRepositoryPort quoteRepository;
	private final QuoteStateTransitionService quoteStateTransitionService;
	private final QuoteCachePort quoteCache;
	private final int draftExpirationMinutes;

	@Autowired
	public DraftExpirationService(
			QuoteRepositoryPort quoteRepository,
			QuoteCachePort quoteCache,
			QuoteProperties quoteProperties) {
		this(
				quoteRepository,
				new QuoteStateTransitionService(),
				quoteCache,
				quoteProperties.draftExpirationMinutes());
	}

	DraftExpirationService(
			QuoteRepositoryPort quoteRepository,
			QuoteStateTransitionService quoteStateTransitionService,
			QuoteCachePort quoteCache,
			int draftExpirationMinutes) {
		this.quoteRepository = quoteRepository;
		this.quoteStateTransitionService = quoteStateTransitionService;
		this.quoteCache = quoteCache;
		this.draftExpirationMinutes = draftExpirationMinutes;
	}

	public int expireStaleDrafts() {
		Instant cutoff = Instant.now().minus(Duration.ofMinutes(draftExpirationMinutes));
		List<Quote> staleDrafts = quoteRepository.findStaleDrafts(cutoff);
		int expiredCount = 0;
		for (Quote draft : staleDrafts) {
			Quote expired = quoteStateTransitionService.markExpired(draft);
			quoteRepository.save(expired);
			quoteCache.evict(draft.getId());
			expiredCount++;
		}
		return expiredCount;
	}
}
