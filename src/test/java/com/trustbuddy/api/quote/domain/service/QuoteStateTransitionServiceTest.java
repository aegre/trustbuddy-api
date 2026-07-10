package com.trustbuddy.api.quote.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuoteStateTransitionServiceTest {

		private QuoteStateTransitionService quoteStateTransitionService;

		@BeforeEach
		void setUp() {
				quoteStateTransitionService = new QuoteStateTransitionService();
		}

		@Test
		void givenDraftQuote_whenMarkSubmitted_thenReturnsSubmittedQuote() {
				// Given
				Quote draft = QuoteGenerator.draft(30);

				// When
				Quote submitted = quoteStateTransitionService.markSubmitted(draft);

				// Then
				assertThat(submitted.getStatus()).isEqualTo(QuoteStatus.SUBMITTED);
		}

		@Test
		void givenSubmissionFailedQuote_whenMarkSubmitted_thenReturnsSubmittedQuote() {
				// Given
				Quote failed = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMISSION_FAILED);

				// When
				Quote submitted = quoteStateTransitionService.markSubmitted(failed);

				// Then
				assertThat(submitted.getStatus()).isEqualTo(QuoteStatus.SUBMITTED);
		}

		@Test
		void givenSubmittedQuote_whenMarkSubmitted_thenReturnsSameQuoteIdempotently() {
				// Given
				Quote submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);

				// When
				Quote result = quoteStateTransitionService.markSubmitted(submitted);

				// Then
				assertThat(result).isSameAs(submitted);
				assertThat(result.getStatus()).isEqualTo(QuoteStatus.SUBMITTED);
		}

		@Test
		void givenExpiredQuote_whenMarkSubmitted_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote expired = QuoteGenerator.draft(30).withStatus(QuoteStatus.EXPIRED);

				// When / Then
				assertThatThrownBy(() -> quoteStateTransitionService.markSubmitted(expired))
								.isInstanceOf(InvalidQuoteStateException.class)
								.hasMessageContaining("submit");
		}

		@Test
		void givenDraftQuote_whenMarkSubmissionFailed_thenReturnsSubmissionFailedQuote() {
				// Given
				Quote draft = QuoteGenerator.draft(30);

				// When
				Quote failed = quoteStateTransitionService.markSubmissionFailed(draft);

				// Then
				assertThat(failed.getStatus()).isEqualTo(QuoteStatus.SUBMISSION_FAILED);
		}

		@Test
		void givenSubmittedQuote_whenMarkSubmissionFailed_thenReturnsSameQuoteIdempotently() {
				// Given
				Quote submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);

				// When
				Quote result = quoteStateTransitionService.markSubmissionFailed(submitted);

				// Then
				assertThat(result).isSameAs(submitted);
		}

		@Test
		void givenDraftQuote_whenMarkExpired_thenReturnsExpiredQuote() {
				// Given
				Quote draft = QuoteGenerator.draft(30);

				// When
				Quote expired = quoteStateTransitionService.markExpired(draft);

				// Then
				assertThat(expired.getStatus()).isEqualTo(QuoteStatus.EXPIRED);
		}

		@Test
		void givenSubmittedQuote_whenMarkExpired_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);

				// When / Then
				assertThatThrownBy(() -> quoteStateTransitionService.markExpired(submitted))
								.isInstanceOf(InvalidQuoteStateException.class)
								.hasMessageContaining("expire");
		}

		@Test
		void givenDraftQuote_whenEnsureCanUpdateCoverage_thenPasses() {
				// Given
				Quote draft = QuoteGenerator.draft(30);

				// When / Then
				quoteStateTransitionService.ensureCanUpdateCoverage(draft);
		}

		@Test
		void givenSubmittedQuote_whenEnsureCanUpdateCoverage_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);

				// When / Then
				assertThatThrownBy(() -> quoteStateTransitionService.ensureCanUpdateCoverage(submitted))
								.isInstanceOf(InvalidQuoteStateException.class)
								.hasMessageContaining("update coverage");
		}
}
