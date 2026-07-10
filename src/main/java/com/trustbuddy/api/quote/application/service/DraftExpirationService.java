package com.trustbuddy.api.quote.application.service;

import com.trustbuddy.api.config.QuoteMetrics;
import com.trustbuddy.api.config.properties.QuoteProperties;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DraftExpirationService {

		private final QuoteRepositoryPort quoteRepository;
		private final QuoteStateTransitionService quoteStateTransitionService;
		private final QuoteMetrics quoteMetrics;
		private final int draftExpirationMinutes;

		@Autowired
		public DraftExpirationService(
						QuoteRepositoryPort quoteRepository,
						QuoteProperties quoteProperties,
						QuoteMetrics quoteMetrics) {
				this(
								quoteRepository,
								new QuoteStateTransitionService(),
								quoteMetrics,
								quoteProperties.draftExpirationMinutes());
		}

		DraftExpirationService(
						QuoteRepositoryPort quoteRepository,
						QuoteStateTransitionService quoteStateTransitionService,
						QuoteMetrics quoteMetrics,
						int draftExpirationMinutes) {
				this.quoteRepository = quoteRepository;
				this.quoteStateTransitionService = quoteStateTransitionService;
				this.quoteMetrics = quoteMetrics;
				this.draftExpirationMinutes = draftExpirationMinutes;
		}

		DraftExpirationService(
						QuoteRepositoryPort quoteRepository,
						QuoteStateTransitionService quoteStateTransitionService,
						int draftExpirationMinutes) {
				this(
								quoteRepository,
								quoteStateTransitionService,
								QuoteMetrics.noop(),
								draftExpirationMinutes);
		}

		public int expireStaleDrafts() {
				Instant cutoff = Instant.now().minus(Duration.ofMinutes(draftExpirationMinutes));
				List<Quote> staleDrafts = quoteRepository.findStaleDrafts(cutoff);
				int expiredCount = 0;
				for (Quote draft : staleDrafts) {
						Quote expired = quoteStateTransitionService.markExpired(draft);
						quoteRepository.save(expired);
						expiredCount++;
				}
				quoteMetrics.recordExpired(expiredCount);
				return expiredCount;
		}
}
