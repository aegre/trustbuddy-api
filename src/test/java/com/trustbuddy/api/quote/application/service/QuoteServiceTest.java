package com.trustbuddy.api.quote.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trustbuddy.api.quote.application.dto.CreateQuoteCommand;
import com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints;
import com.trustbuddy.api.quote.application.dto.UpdateCoverageCommand;
import com.trustbuddy.api.quote.application.dto.UpdatePersonalInfoCommand;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
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

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

		@Mock private QuoteRepositoryPort quoteRepository;

		@Mock private QuoteCachePort quoteCache;

		private QuoteService quoteService;

		@BeforeEach
		void setUp() {
				quoteService = new QuoteService(quoteRepository, quoteCache);
		}

		@Test
		void givenPersonalInfo_whenCreateQuote_thenSavesDraftQuote() {
				// Given
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote created =
								quoteService.createQuote(
												createCommand(
																"Jane Doe",
																"jane@example.com",
																30,
																QuoteFieldConstraints.ZIP_CODE_EXAMPLE));

				// Then
				assertThat(created.getStatus()).isEqualTo(QuoteStatus.DRAFT);
				assertThat(created.getName()).isEqualTo("Jane Doe");
				verify(quoteRepository).save(any(Quote.class));
		}

		@Test
		void givenInvalidCreateCommand_whenCreateQuote_thenThrowsQuoteValidationException() {
				// Given
				CreateQuoteCommand command = createCommand("", "not-an-email", 0, "abc");

				// When / Then
				assertThatThrownBy(() -> quoteService.createQuote(command))
								.isInstanceOf(QuoteValidationException.class);
				verify(quoteRepository, never()).save(any());
		}

		@Test
		void givenDraftQuote_whenUpdatePersonalInfo_thenSavesUpdatedFields() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote updated =
								quoteService.updatePersonalInfo(
												draft.getId(),
												updatePersonalInfoCommand(
																"Jane Updated", "updated@example.com", 35, "90210"));

				// Then
				assertThat(updated.getId()).isEqualTo(draft.getId());
				assertThat(updated.getName()).isEqualTo("Jane Updated");
				assertThat(updated.getEmail()).isEqualTo("updated@example.com");
				assertThat(updated.getAge()).isEqualTo(35);
				assertThat(updated.getZipCode()).isEqualTo("90210");
		}

		@Test
		void givenQuoteWithCoverage_whenUpdatePersonalInfo_thenRecalculatesPremium() {
				// Given
				Quote draft =
								QuoteGenerator.coverage(30, CoverageType.STANDARD)
												.usesTobacco(false)
												.needsSpouseCoverage(false)
												.build();
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote updated =
								quoteService.updatePersonalInfo(
												draft.getId(),
												updatePersonalInfoCommand(
																draft.getName(), draft.getEmail(), 70, draft.getZipCode()));

				// Then
				assertThat(updated.getAge()).isEqualTo(70);
				assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("150.00");
		}

		@Test
		void
						givenSeniorQuoteWithHealthFields_whenUpdatePersonalInfoToYoungerAge_thenClearsSeniorFieldsAndRecalculatesPremium() {
				// Given
				Quote draft =
								QuoteGenerator.coverage(70, CoverageType.STANDARD)
												.hasPreexistingConditions(true)
												.conditions(ConditionType.DIABETES)
												.takesPrescriptionMedication(false)
												.usesTobacco(false)
												.needsSpouseCoverage(false)
												.build();
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote updated =
								quoteService.updatePersonalInfo(
												draft.getId(),
												updatePersonalInfoCommand(
																draft.getName(), draft.getEmail(), 40, draft.getZipCode()));

				// Then
				assertThat(updated.getAge()).isEqualTo(40);
				assertThat(updated.getHasPreexistingConditions()).isNull();
				assertThat(updated.getConditions()).isEmpty();
				assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("100.00");
		}

		@Test
		void givenSubmittedQuote_whenUpdatePersonalInfo_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);
				when(quoteCache.get(submitted.getId())).thenReturn(Optional.of(submitted));

				// When / Then
				assertThatThrownBy(
												() ->
																quoteService.updatePersonalInfo(
																				submitted.getId(),
																				updatePersonalInfoCommand(
																								"Jane Doe",
																								"jane@example.com",
																								30,
																								QuoteFieldConstraints.ZIP_CODE_EXAMPLE)))
								.isInstanceOf(InvalidQuoteStateException.class);
				verify(quoteRepository, never()).save(any());
		}

		@Test
		void
						givenQuoteWithCoverage_whenUpdateCoverageWithSingleField_thenMergesAndRecalculatesPremium() {
				// Given
				Quote draft =
								QuoteGenerator.coverage(30, CoverageType.STANDARD)
												.usesTobacco(false)
												.needsSpouseCoverage(false)
												.build();
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));
				UpdateCoverageCommand command = new UpdateCoverageCommand();
				command.setUsesTobacco(true);

				// When
				Quote updated = quoteService.updateCoverage(draft.getId(), command);

				// Then
				assertThat(updated.getCoverageType()).isEqualTo(CoverageType.STANDARD);
				assertThat(updated.getUsesTobacco()).isTrue();
				assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("120.00");
		}

		@Test
		void
						givenQuoteWithoutCoverage_whenUpdateCoverageWithoutCoverageType_thenThrowsQuoteValidationException() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				UpdateCoverageCommand command = new UpdateCoverageCommand();
				command.setUsesTobacco(true);

				// When / Then
				assertThatThrownBy(() -> quoteService.updateCoverage(draft.getId(), command))
								.isInstanceOf(QuoteValidationException.class)
								.hasMessageContaining("coverageType is required");
				verify(quoteRepository, never()).save(any());
		}

		@Test
		void givenDraftQuote_whenUpdateCoverageWithEmptyCommand_thenReturnsQuoteUnchanged() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.of(draft));

				// When
				Quote updated = quoteService.updateCoverage(draft.getId(), new UpdateCoverageCommand());

				// Then
				assertThat(updated).isEqualTo(draft);
				verify(quoteRepository, never()).save(any());
		}

		@Test
		void givenDraftQuote_whenUpdateCoverageWithOptionalFieldsOmitted_thenSavesCoverage() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote updated =
								quoteService.updateCoverage(draft.getId(), updateCommand(CoverageType.STANDARD));

				// Then
				assertThat(updated.getCoverageType()).isEqualTo(CoverageType.STANDARD);
				assertThat(updated.getTakesPrescriptionMedication()).isNull();
				assertThat(updated.getUsesTobacco()).isNull();
				assertThat(updated.getNeedsSpouseCoverage()).isNull();
				assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("100.00");
		}

		@Test
		void givenYoungQuote_whenUpdateCoverageWithTobaccoAnswer_thenRecalculatesPremium() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));
				UpdateCoverageCommand command = updateCommand(CoverageType.STANDARD);
				command.setTakesPrescriptionMedication(false);
				command.setUsesTobacco(true);
				command.setNeedsSpouseCoverage(false);

				// When
				Quote updated = quoteService.updateCoverage(draft.getId(), command);

				// Then
				assertThat(updated.getUsesTobacco()).isTrue();
				assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("120.00");
		}

		@Test
		void givenDraftQuote_whenUpdateCoverage_thenRecalculatesPremiumAndSaves() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));

				// When
				Quote updated =
								quoteService.updateCoverage(draft.getId(), updateCommand(CoverageType.STANDARD));

				// Then
				assertThat(updated.getCoverageType()).isEqualTo(CoverageType.STANDARD);
				assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("100.00");
		}

		@Test
		void givenSubmittedQuote_whenUpdateCoverage_thenThrowsInvalidQuoteStateException() {
				// Given
				Quote submitted = QuoteGenerator.draft(30).withStatus(QuoteStatus.SUBMITTED);
				when(quoteCache.get(submitted.getId())).thenReturn(Optional.of(submitted));

				// When / Then
				assertThatThrownBy(
												() ->
																quoteService.updateCoverage(
																				submitted.getId(), updateCommand(CoverageType.STANDARD)))
								.isInstanceOf(InvalidQuoteStateException.class);
				verify(quoteRepository, never()).findById(any());
		}

		@Test
		void givenUnknownId_whenGetQuote_thenThrowsQuoteNotFoundException() {
				// Given
				UUID id = UUID.randomUUID();
				when(quoteCache.get(id)).thenReturn(Optional.empty());
				when(quoteRepository.findById(id)).thenReturn(Optional.empty());

				// When / Then
				assertThatThrownBy(() -> quoteService.getQuote(id))
								.isInstanceOf(QuoteNotFoundException.class);
				verify(quoteCache, never()).put(any());
		}

		@Test
		void givenCachedQuote_whenGetQuote_thenReturnsFromCacheWithoutRepositoryLookup() {
				// Given
				Quote cached = QuoteGenerator.draft(30);
				when(quoteCache.get(cached.getId())).thenReturn(Optional.of(cached));

				// When
				Quote result = quoteService.getQuote(cached.getId());

				// Then
				assertThat(result).isEqualTo(cached);
				verify(quoteRepository, never()).findById(any());
				verify(quoteCache, never()).put(any());
		}

		@Test
		void givenUncachedQuote_whenGetQuote_thenLoadsFromRepositoryAndCaches() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

				// When
				Quote result = quoteService.getQuote(draft.getId());

				// Then
				assertThat(result).isEqualTo(draft);
				verify(quoteCache).put(draft);
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
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));
				UpdateCoverageCommand command = updateCommand(CoverageType.STANDARD);
				command.setHasPreexistingConditions(true);
				command.setConditions(Set.of(ConditionType.DIABETES));
				command.setTakesPrescriptionMedication(false);
				command.setUsesTobacco(true);
				command.setNeedsSpouseCoverage(true);

				// When
				Quote updated = quoteService.updateCoverage(draft.getId(), command);

				// Then
				assertThat(updated.getHasPreexistingConditions()).isTrue();
				assertThat(updated.getConditions()).containsExactly(ConditionType.DIABETES);
				assertThat(updated.getEstimatedMonthlyPremium()).isEqualByComparingTo("327.60");
		}

		@Test
		void givenDraftQuote_whenUpdateCoverage_thenPersistsQuoteWithSameId() {
				// Given
				Quote draft = QuoteGenerator.draft(30);
				when(quoteCache.get(draft.getId())).thenReturn(Optional.empty());
				when(quoteRepository.findById(draft.getId())).thenReturn(Optional.of(draft));
				when(quoteRepository.save(any(Quote.class)))
								.thenAnswer(invocation -> invocation.getArgument(0));
				ArgumentCaptor<Quote> savedQuote = ArgumentCaptor.forClass(Quote.class);

				// When
				quoteService.updateCoverage(draft.getId(), updateCommand(CoverageType.BASIC));

				// Then
				verify(quoteRepository).save(savedQuote.capture());
				assertThat(savedQuote.getValue().getId()).isEqualTo(draft.getId());
				assertThat(savedQuote.getValue().getEstimatedMonthlyPremium())
								.isEqualByComparingTo(new BigDecimal("50.00"));
		}

		private static CreateQuoteCommand createCommand(
						String name, String email, int age, String zipCode) {
				CreateQuoteCommand command = new CreateQuoteCommand();
				command.setName(name);
				command.setEmail(email);
				command.setAge(age);
				command.setZipCode(zipCode);
				return command;
		}

		private static UpdateCoverageCommand updateCommand(CoverageType coverageType) {
				UpdateCoverageCommand command = new UpdateCoverageCommand();
				command.setCoverageType(coverageType);
				return command;
		}

		private static UpdatePersonalInfoCommand updatePersonalInfoCommand(
						String name, String email, int age, String zipCode) {
				UpdatePersonalInfoCommand command = new UpdatePersonalInfoCommand();
				command.setName(name);
				command.setEmail(email);
				command.setAge(age);
				command.setZipCode(zipCode);
				return command;
		}
}
