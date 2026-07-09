package com.trustbuddy.api.quote.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;

@ExtendWith(MockitoExtension.class)
class DraftExpirationServiceTest {

	private static final int DRAFT_EXPIRATION_MINUTES = 30;

	@Mock
	private QuoteRepositoryPort quoteRepository;

	private DraftExpirationService draftExpirationService;

	@BeforeEach
	void setUp() {
		draftExpirationService = new DraftExpirationService(
				quoteRepository,
				new QuoteStateTransitionService(),
				DRAFT_EXPIRATION_MINUTES);
	}

	@Test
	void givenStaleDrafts_whenExpireStaleDrafts_thenMarksExpiredAndSaves() {
		// Given
		Quote staleDraft = QuoteGenerator.draft(30).withStatus(QuoteStatus.DRAFT);
		when(quoteRepository.findStaleDrafts(any())).thenReturn(List.of(staleDraft));
		when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		int expiredCount = draftExpirationService.expireStaleDrafts();

		// Then
		assertThat(expiredCount).isEqualTo(1);
		ArgumentCaptor<Quote> savedCaptor = ArgumentCaptor.forClass(Quote.class);
		verify(quoteRepository).save(savedCaptor.capture());
		assertThat(savedCaptor.getValue().getStatus()).isEqualTo(QuoteStatus.EXPIRED);
	}

	@Test
	void givenNoStaleDrafts_whenExpireStaleDrafts_thenReturnsZero() {
		// Given
		when(quoteRepository.findStaleDrafts(any())).thenReturn(List.of());

		// When
		int expiredCount = draftExpirationService.expireStaleDrafts();

		// Then
		assertThat(expiredCount).isZero();
		verify(quoteRepository, never()).save(any());
	}
}
