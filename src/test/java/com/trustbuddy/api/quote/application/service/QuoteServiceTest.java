package com.trustbuddy.api.quote.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.domain.service.CoverageHealthPolicy;
import com.trustbuddy.api.quote.domain.service.PremiumCalculator;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

	@Mock
	private QuoteRepositoryPort quoteRepository;

	private QuoteService quoteService;

	@BeforeEach
	void setUp() {
		quoteService = new QuoteService(
				quoteRepository,
				new PremiumCalculator(),
				new CoverageHealthPolicy(),
				new QuoteStateTransitionService());
	}

	@Test
	void givenPersonalInfo_whenCreateQuote_thenSavesDraftQuote() {
		// Given
		when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Quote created = quoteService.createQuote("Jane Doe", "jane@example.com", 30, "12345");

		// Then
		assertThat(created.getStatus()).isEqualTo(QuoteStatus.DRAFT);
		assertThat(created.getName()).isEqualTo("Jane Doe");
		verify(quoteRepository).save(any(Quote.class));
	}

	@Test
	void givenDraftQuote_whenUpdateCoverageWithOptionalFieldsOmitted_thenSavesCoverage() {
		// Given
		Quote draft = QuoteGenerator.draft(30);
		when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
		when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Quote updated = quoteService.updateCoverage(
				draft.getId(),
				CoverageType.STANDARD,
				null,
				null,
				null,
				null,
				null);

		// Then
		assertThat(updated.getCoverageType()).isEqualTo(CoverageType.STANDARD);
		assertThat(updated.getTakesPrescriptionMedication()).isNull();
		assertThat(updated.getUsesTobacco()).isNull();
		assertThat(updated.getNeedsSpouseCoverage()).isNull();
		assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("100.00");
	}

	@Test
	void givenDraftQuote_whenUpdateCoverage_thenRecalculatesPremiumAndSaves() {
		// Given
		Quote draft = QuoteGenerator.draft(30);
		when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
		when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Quote updated = quoteService.updateCoverage(
				draft.getId(),
				CoverageType.STANDARD,
				null,
				null,
				false,
				false,
				false);

		// Then
		assertThat(updated.getCoverageType()).isEqualTo(CoverageType.STANDARD);
		assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("100.00");
	}

	@Test
	void givenSubmittedQuote_whenUpdateCoverage_thenThrowsInvalidQuoteStateException() {
		// Given
		Quote submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);
		when(quoteRepository.findById(submitted.getId())).thenReturn(Optional.of(submitted));

		// When / Then
		assertThatThrownBy(() -> quoteService.updateCoverage(
				submitted.getId(),
				CoverageType.STANDARD,
				null,
				null,
				false,
				false,
				false))
				.isInstanceOf(InvalidQuoteStateException.class);
	}

	@Test
	void givenUnknownId_whenGetQuote_thenThrowsQuoteNotFoundException() {
		// Given
		UUID id = UUID.randomUUID();
		when(quoteRepository.findById(id)).thenReturn(Optional.empty());

		// When / Then
		assertThatThrownBy(() -> quoteService.getQuote(id))
				.isInstanceOf(QuoteNotFoundException.class);
	}

	@Test
	void givenPageable_whenListQuotes_thenReturnsRepositoryPage() {
		// Given
		Quote draft = QuoteGenerator.draft(30);
		Page<Quote> page = new PageImpl<>(java.util.List.of(draft));
		when(quoteRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

		// When
		Page<Quote> result = quoteService.listQuotes(PageRequest.of(0, 20));

		// Then
		assertThat(result.getContent()).containsExactly(draft);
	}

	@Test
	void givenSeniorQuote_whenUpdateCoverage_thenAppliesHealthFieldsAndPremium() {
		// Given
		Quote draft = QuoteGenerator.draft(70);
		when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
		when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Quote updated = quoteService.updateCoverage(
				draft.getId(),
				CoverageType.STANDARD,
				true,
				Set.of(ConditionType.DIABETES),
				false,
				true,
				true);

		// Then
		assertThat(updated.getHasPreexistingConditions()).isTrue();
		assertThat(updated.getConditions()).containsExactly(ConditionType.DIABETES);
		assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("327.60");
	}

	@Test
	void givenDraftQuote_whenUpdateCoverage_thenPersistsQuoteWithSameId() {
		// Given
		Quote draft = QuoteGenerator.draft(30);
		when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
		when(quoteRepository.save(any(Quote.class))).thenAnswer(invocation -> invocation.getArgument(0));
		ArgumentCaptor<Quote> savedQuote = ArgumentCaptor.forClass(Quote.class);

		// When
		quoteService.updateCoverage(
				draft.getId(),
				CoverageType.BASIC,
				null,
				null,
				false,
				false,
				false);

		// Then
		verify(quoteRepository).save(savedQuote.capture());
		assertThat(savedQuote.getValue().getId()).isEqualTo(draft.getId());
		assertThat(savedQuote.getValue().getEstimatedMonthlyPremium()).isEqualByComparingTo(new BigDecimal("50.00"));
	}
}
