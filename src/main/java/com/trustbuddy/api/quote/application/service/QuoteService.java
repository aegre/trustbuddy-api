package com.trustbuddy.api.quote.application.service;

import com.trustbuddy.api.quote.application.dto.CreateQuoteCommand;
import com.trustbuddy.api.quote.application.dto.UpdateCoverageCommand;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.application.validation.CommandValidator;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageDetails;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.service.CoverageHealthPolicy;
import com.trustbuddy.api.quote.domain.service.PremiumCalculator;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuoteService {

		private final QuoteRepositoryPort quoteRepository;
		private final QuoteCachePort quoteCache;
		private final PremiumCalculator premiumCalculator;
		private final CoverageHealthPolicy coverageHealthPolicy;
		private final QuoteStateTransitionService quoteStateTransitionService;
		private final CommandValidator commandValidator;

		@Autowired
		public QuoteService(
						QuoteRepositoryPort quoteRepository,
						QuoteCachePort quoteCache,
						CommandValidator commandValidator) {
				this(
								quoteRepository,
								quoteCache,
								new PremiumCalculator(),
								new CoverageHealthPolicy(),
								new QuoteStateTransitionService(),
								commandValidator);
		}

		QuoteService(
						QuoteRepositoryPort quoteRepository,
						QuoteCachePort quoteCache,
						PremiumCalculator premiumCalculator,
						CoverageHealthPolicy coverageHealthPolicy,
						QuoteStateTransitionService quoteStateTransitionService,
						CommandValidator commandValidator) {
				this.quoteRepository = quoteRepository;
				this.quoteCache = quoteCache;
				this.premiumCalculator = premiumCalculator;
				this.coverageHealthPolicy = coverageHealthPolicy;
				this.quoteStateTransitionService = quoteStateTransitionService;
				this.commandValidator = commandValidator;
		}

		QuoteService(QuoteRepositoryPort quoteRepository, QuoteCachePort quoteCache) {
				this(
								quoteRepository,
								quoteCache,
								new PremiumCalculator(),
								new CoverageHealthPolicy(),
								new QuoteStateTransitionService(),
								new CommandValidator(Validation.buildDefaultValidatorFactory().getValidator()));
		}

		public Quote createQuote(CreateQuoteCommand command) {
				commandValidator.validate(command);
				return quoteRepository.save(
								Quote.createDraft(
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
								quote.getAge(), command.getHasPreexistingConditions(), command.getConditions());

				Set<ConditionType> normalizedConditions =
								command.getConditions() == null ? Set.of() : command.getConditions();
				CoverageDetails coverageDetails =
								toCoverageDetails(command, normalizedConditions, BigDecimal.ZERO);
				BigDecimal premium = premiumCalculator.calculate(quote.applyCoverage(coverageDetails));

				return quoteRepository.save(quote.applyCoverage(coverageDetails.withPremium(premium)));
		}

		public Quote getQuote(UUID id) {
				Optional<Quote> cached = quoteCache.get(id);
				if (cached.isPresent()) {
						return cached.get();
				}

				Quote quote =
								quoteRepository.findById(id).orElseThrow(() -> new QuoteNotFoundException(id));
				quoteCache.put(quote);
				return quote;
		}

		public Page<Quote> listQuotes(Pageable pageable) {
				return quoteRepository.findAll(pageable);
		}

		private CoverageDetails toCoverageDetails(
						UpdateCoverageCommand command, Set<ConditionType> conditions, BigDecimal premium) {
				return new CoverageDetails(
								command.getCoverageType(),
								command.getHasPreexistingConditions(),
								conditions,
								command.getTakesPrescriptionMedication(),
								command.getUsesTobacco(),
								command.getNeedsSpouseCoverage(),
								premium);
		}
}
