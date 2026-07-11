package com.trustbuddy.api.quote.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trustbuddy.api.quote.application.port.out.InsurerGatewayPort;
import com.trustbuddy.api.quote.application.port.out.InsurerSubmissionResult;
import com.trustbuddy.api.quote.application.port.out.QuoteEventPublisherPort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.exception.ExternalSubmissionException;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteErrorCodes;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuoteSubmissionServiceTest {

		@Mock private QuoteRepositoryPort quoteRepository;

		@Mock private InsurerGatewayPort insurerGateway;

		@Mock private QuoteEventPublisherPort quoteEventPublisher;

		private QuoteSubmissionService quoteSubmissionService;

		@BeforeEach
		void setUp() {
				quoteSubmissionService =
								new QuoteSubmissionService(quoteRepository, insurerGateway, quoteEventPublisher);
		}

		@Test
		void givenCoveredDraftQuote_whenSubmitQuote_thenMarksSubmittedAndReturnsQuote() {
				// Given
				Quote draft = coveredQuote(QuoteStatus.DRAFT);
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(insurerGateway.submit(draft)).thenReturn(new InsurerSubmissionResult(true, 200, "ok"));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote submitted = quoteSubmissionService.submitQuote(draft.getId());

				// Then
				assertThat(submitted.getStatus()).isEqualTo(QuoteStatus.SUBMITTED);
				verify(insurerGateway).submit(draft);
				verify(quoteEventPublisher).publishQuoteSubmitted(submitted);
		}

		@Test
		void givenSubmittedQuote_whenSubmitQuote_thenReturnsSameQuoteWithoutCallingGateway() {
				// Given
				Quote submitted = coveredQuote(QuoteStatus.SUBMITTED);
				when(quoteRepository.findById(submitted.getId())).thenReturn(Optional.of(submitted));

				// When
				Quote result = quoteSubmissionService.submitQuote(submitted.getId());

				// Then
				assertThat(result).isSameAs(submitted);
				verify(insurerGateway, never()).submit(any());
				verify(quoteRepository, never()).save(any());
				verify(quoteEventPublisher, never()).publishQuoteSubmitted(any());
		}

		@Test
		void givenExpiredQuote_whenSubmitQuote_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote expired = coveredQuote(QuoteStatus.EXPIRED);
				when(quoteRepository.findById(expired.getId())).thenReturn(Optional.of(expired));

				// When / Then
				assertThatThrownBy(() -> quoteSubmissionService.submitQuote(expired.getId()))
								.isInstanceOf(InvalidQuoteStateException.class)
								.hasFieldOrPropertyWithValue("errorCode", QuoteErrorCodes.QUOTE_EXPIRED);
				verify(insurerGateway, never()).submit(any());
		}

		@Test
		void
						givenGatewayFailure_whenSubmitQuote_thenMarksSubmissionFailedAndThrowsExternalSubmissionException() {
				// Given
				Quote draft = coveredQuote(QuoteStatus.DRAFT);
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(insurerGateway.submit(draft))
								.thenReturn(new InsurerSubmissionResult(false, 500, "gateway down"));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When / Then
				assertThatThrownBy(() -> quoteSubmissionService.submitQuote(draft.getId()))
								.isInstanceOf(ExternalSubmissionException.class)
								.hasMessageContaining("gateway down");

				verify(quoteRepository).save(any(Quote.class));
				verify(quoteEventPublisher, never()).publishQuoteSubmitted(any());
		}

		@Test
		void givenSubmissionFailedQuote_whenSubmitQuote_thenCanResubmitSuccessfully() {
				// Given
				Quote failed = coveredQuote(QuoteStatus.SUBMISSION_FAILED);
				when(quoteRepository.findById(failed.getId())).thenReturn(Optional.of(failed));
				when(insurerGateway.submit(failed))
								.thenReturn(new InsurerSubmissionResult(true, 200, "ok"));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote submitted = quoteSubmissionService.submitQuote(failed.getId());

				// Then
				assertThat(submitted.getStatus()).isEqualTo(QuoteStatus.SUBMITTED);
				verify(quoteEventPublisher).publishQuoteSubmitted(submitted);
		}

		@Test
		void
						givenQuoteWithoutTakesPrescriptionMedication_whenSubmitQuote_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote draft =
								QuoteGenerator.readyForSubmissionWithoutTakesPrescriptionMedication(30)
												.withStatus(QuoteStatus.DRAFT);
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

				// When / Then
				assertThatThrownBy(() -> quoteSubmissionService.submitQuote(draft.getId()))
								.isInstanceOf(InvalidQuoteStateException.class)
								.hasFieldOrPropertyWithValue(
												"errorCode", QuoteErrorCodes.QUOTE_MISSING_HEALTH_FIELDS)
								.hasMessageContaining("takesPrescriptionMedication is required");
				verify(insurerGateway, never()).submit(any());
		}

		@Test
		void
						givenSeniorQuoteWithoutRequiredHealthAnswer_whenSubmitQuote_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote draft =
								QuoteGenerator.coverage(70, CoverageType.STANDARD)
												.takesPrescriptionMedication(false)
												.usesTobacco(false)
												.needsSpouseCoverage(false)
												.build()
												.withStatus(QuoteStatus.DRAFT);
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

				// When / Then
				assertThatThrownBy(() -> quoteSubmissionService.submitQuote(draft.getId()))
								.isInstanceOf(InvalidQuoteStateException.class)
								.hasFieldOrPropertyWithValue(
												"errorCode", QuoteErrorCodes.QUOTE_MISSING_HEALTH_FIELDS)
								.hasMessageContaining("hasPreexistingConditions is required");
				verify(insurerGateway, never()).submit(any());
		}

		@Test
		void givenQuoteWithoutCoverage_whenSubmitQuote_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote draft =
								QuoteGenerator.readyForSubmissionWithoutCoverage(30).withStatus(QuoteStatus.DRAFT);
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

				// When / Then
				assertThatThrownBy(() -> quoteSubmissionService.submitQuote(draft.getId()))
								.isInstanceOf(InvalidQuoteStateException.class)
								.hasFieldOrPropertyWithValue(
												"errorCode", QuoteErrorCodes.QUOTE_MISSING_COVERAGE)
								.hasMessageContaining("coverage data");
		}

		private static Quote coveredQuote(QuoteStatus status) {
				return QuoteGenerator.readyForSubmission(30).withStatus(status);
		}
}
