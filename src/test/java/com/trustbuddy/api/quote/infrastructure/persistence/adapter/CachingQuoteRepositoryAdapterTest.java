package com.trustbuddy.api.quote.infrastructure.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class CachingQuoteRepositoryAdapterTest {

		@Mock private QuotePersistenceAdapter delegate;

		@Mock private QuoteCachePort quoteCache;

		@Mock private ObjectProvider<QuoteCachePort> quoteCacheProvider;

		private CachingQuoteRepositoryAdapter cachingQuoteRepository;

		@BeforeEach
		void setUp() {
				cachingQuoteRepository = new CachingQuoteRepositoryAdapter(delegate, quoteCacheProvider);
		}

		@Test
		void givenQuote_whenSave_thenDelegatesAndEvictsCache() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCacheProvider.getObject()).thenReturn(quoteCache);
				when(delegate.save(draft)).thenReturn(draft);

				// When
				Quote saved = cachingQuoteRepository.save(draft);

				// Then
				assertThat(saved).isEqualTo(draft);
				verify(delegate).save(draft);
				verify(quoteCache).evict(draft.getId());
		}

		@Test
		void givenId_whenFindById_thenDelegatesWithoutCacheInteraction() {
				// Given
				UUID id = UUID.randomUUID();
				Quote draft = QuoteGenerator.draft(30);
				when(delegate.findById(id)).thenReturn(Optional.of(draft));

				// When
				Optional<Quote> found = cachingQuoteRepository.findById(id);

				// Then
				assertThat(found).contains(draft);
				verify(delegate).findById(id);
		}
}
