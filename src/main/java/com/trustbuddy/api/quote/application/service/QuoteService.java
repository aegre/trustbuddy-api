package com.trustbuddy.api.quote.application.service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.service.CoverageHealthPolicy;
import com.trustbuddy.api.quote.domain.service.PremiumCalculator;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;

@Service
public class QuoteService {

	private final QuoteRepositoryPort quoteRepository;
	private final PremiumCalculator premiumCalculator;
	private final CoverageHealthPolicy coverageHealthPolicy;
	private final QuoteStateTransitionService quoteStateTransitionService;

	public QuoteService(QuoteRepositoryPort quoteRepository) {
		this(
				quoteRepository,
				new PremiumCalculator(),
				new CoverageHealthPolicy(),
				new QuoteStateTransitionService());
	}

	QuoteService(
			QuoteRepositoryPort quoteRepository,
			PremiumCalculator premiumCalculator,
			CoverageHealthPolicy coverageHealthPolicy,
			QuoteStateTransitionService quoteStateTransitionService) {
		this.quoteRepository = quoteRepository;
		this.premiumCalculator = premiumCalculator;
		this.coverageHealthPolicy = coverageHealthPolicy;
		this.quoteStateTransitionService = quoteStateTransitionService;
	}

	public Quote createQuote(String name, String email, int age, String zipCode) {
		return quoteRepository.save(Quote.createDraft(name, email, age, zipCode));
	}

	public Quote updateCoverage(
			UUID id,
			CoverageType coverageType,
			Boolean hasPreexistingConditions,
			Set<ConditionType> conditions,
			Boolean takesPrescriptionMedication,
			Boolean usesTobacco,
			Boolean needsSpouseCoverage) {
		Quote quote = getQuote(id);
		quoteStateTransitionService.ensureCanUpdateCoverage(quote);
		validateRequiredCoverageAnswers(takesPrescriptionMedication, usesTobacco, needsSpouseCoverage);
		coverageHealthPolicy.validateHealthFieldsForAge(
				quote.getAge(),
				hasPreexistingConditions,
				conditions);

		Set<ConditionType> normalizedConditions = conditions == null ? Set.of() : conditions;
		Quote forPricing = quote.applyCoverage(
				coverageType,
				hasPreexistingConditions,
				normalizedConditions,
				takesPrescriptionMedication,
				usesTobacco,
				needsSpouseCoverage,
				BigDecimal.ZERO);
		BigDecimal premium = premiumCalculator.calculate(forPricing);
		Quote withCoverage = forPricing.applyCoverage(
				coverageType,
				hasPreexistingConditions,
				normalizedConditions,
				takesPrescriptionMedication,
				usesTobacco,
				needsSpouseCoverage,
				premium);

		return quoteRepository.save(withCoverage);
	}

	public Quote getQuote(UUID id) {
		return quoteRepository.findById(id)
				.orElseThrow(() -> new QuoteNotFoundException(id));
	}

	public Page<Quote> listQuotes(Pageable pageable) {
		return quoteRepository.findAll(pageable);
	}

	private void validateRequiredCoverageAnswers(
			Boolean takesPrescriptionMedication,
			Boolean usesTobacco,
			Boolean needsSpouseCoverage) {
		if (takesPrescriptionMedication == null) {
			throw new QuoteValidationException("takesPrescriptionMedication is required");
		}
		if (usesTobacco == null) {
			throw new QuoteValidationException("usesTobacco is required");
		}
		if (needsSpouseCoverage == null) {
			throw new QuoteValidationException("needsSpouseCoverage is required");
		}
	}
}
