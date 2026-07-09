package com.trustbuddy.api.quote.application.service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.trustbuddy.api.quote.application.dto.CreateQuoteCommand;
import com.trustbuddy.api.quote.application.dto.UpdateCoverageCommand;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.application.validation.CommandValidator;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.service.CoverageHealthPolicy;
import com.trustbuddy.api.quote.domain.service.PremiumCalculator;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;

import jakarta.validation.Validation;

@Service
public class QuoteService {

	private final QuoteRepositoryPort quoteRepository;
	private final PremiumCalculator premiumCalculator;
	private final CoverageHealthPolicy coverageHealthPolicy;
	private final QuoteStateTransitionService quoteStateTransitionService;
	private final CommandValidator commandValidator;

	@Autowired
	public QuoteService(QuoteRepositoryPort quoteRepository, CommandValidator commandValidator) {
		this(
				quoteRepository,
				new PremiumCalculator(),
				new CoverageHealthPolicy(),
				new QuoteStateTransitionService(),
				commandValidator);
	}

	QuoteService(
			QuoteRepositoryPort quoteRepository,
			PremiumCalculator premiumCalculator,
			CoverageHealthPolicy coverageHealthPolicy,
			QuoteStateTransitionService quoteStateTransitionService,
			CommandValidator commandValidator) {
		this.quoteRepository = quoteRepository;
		this.premiumCalculator = premiumCalculator;
		this.coverageHealthPolicy = coverageHealthPolicy;
		this.quoteStateTransitionService = quoteStateTransitionService;
		this.commandValidator = commandValidator;
	}

	QuoteService(QuoteRepositoryPort quoteRepository) {
		this(
				quoteRepository,
				new PremiumCalculator(),
				new CoverageHealthPolicy(),
				new QuoteStateTransitionService(),
				new CommandValidator(Validation.buildDefaultValidatorFactory().getValidator()));
	}

	public Quote createQuote(CreateQuoteCommand command) {
		commandValidator.validate(command);
		return quoteRepository.save(Quote.createDraft(
				command.getName(),
				command.getEmail(),
				command.getAge(),
				command.getZipCode()));
	}

	public Quote updateCoverage(UUID id, UpdateCoverageCommand command) {
		commandValidator.validate(command);
		Quote quote = getQuote(id);
		quoteStateTransitionService.ensureCanUpdateCoverage(quote);
		coverageHealthPolicy.validateHealthFieldsForAge(
				quote.getAge(),
				command.getHasPreexistingConditions(),
				command.getConditions());

		Set<ConditionType> normalizedConditions =
				command.getConditions() == null ? Set.of() : command.getConditions();
		Quote forPricing = quote.applyCoverage(
				command.getCoverageType(),
				command.getHasPreexistingConditions(),
				normalizedConditions,
				command.getTakesPrescriptionMedication(),
				command.getUsesTobacco(),
				command.getNeedsSpouseCoverage(),
				BigDecimal.ZERO);
		BigDecimal premium = premiumCalculator.calculate(forPricing);
		Quote withCoverage = forPricing.applyCoverage(
				command.getCoverageType(),
				command.getHasPreexistingConditions(),
				normalizedConditions,
				command.getTakesPrescriptionMedication(),
				command.getUsesTobacco(),
				command.getNeedsSpouseCoverage(),
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
}
