package com.trustbuddy.api.quote.application.service;

import com.trustbuddy.api.quote.application.dto.CreateQuoteCommand;
import com.trustbuddy.api.quote.application.dto.UpdateCoverageCommand;
import com.trustbuddy.api.quote.application.dto.UpdatePersonalInfoCommand;
import com.trustbuddy.api.quote.application.dto.UpdatePromoCommand;
import com.trustbuddy.api.quote.application.port.out.PromotionRepositoryPort;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.application.validation.CommandValidator;
import com.trustbuddy.api.quote.domain.exception.QuoteErrorCodes;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.AppliedPromotion;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageDetails;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Promotion;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.service.CoverageHealthPolicy;
import com.trustbuddy.api.quote.domain.service.PremiumCalculator;
import com.trustbuddy.api.quote.domain.service.PromotionDiscountCalculator;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.time.Instant;
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
		private final PromotionRepositoryPort promotionRepository;
		private final PremiumCalculator premiumCalculator;
		private final PromotionDiscountCalculator promotionDiscountCalculator;
		private final CoverageHealthPolicy coverageHealthPolicy;
		private final QuoteStateTransitionService quoteStateTransitionService;
		private final CommandValidator commandValidator;

		@Autowired
		public QuoteService(
						QuoteRepositoryPort quoteRepository,
						QuoteCachePort quoteCache,
						PromotionRepositoryPort promotionRepository,
						CommandValidator commandValidator) {
				this(
								quoteRepository,
								quoteCache,
								promotionRepository,
								new PremiumCalculator(),
								new PromotionDiscountCalculator(),
								new CoverageHealthPolicy(),
								new QuoteStateTransitionService(),
								commandValidator);
		}

		QuoteService(
						QuoteRepositoryPort quoteRepository,
						QuoteCachePort quoteCache,
						PromotionRepositoryPort promotionRepository,
						PremiumCalculator premiumCalculator,
						PromotionDiscountCalculator promotionDiscountCalculator,
						CoverageHealthPolicy coverageHealthPolicy,
						QuoteStateTransitionService quoteStateTransitionService,
						CommandValidator commandValidator) {
				this.quoteRepository = quoteRepository;
				this.quoteCache = quoteCache;
				this.promotionRepository = promotionRepository;
				this.premiumCalculator = premiumCalculator;
				this.promotionDiscountCalculator = promotionDiscountCalculator;
				this.coverageHealthPolicy = coverageHealthPolicy;
				this.quoteStateTransitionService = quoteStateTransitionService;
				this.commandValidator = commandValidator;
		}

		QuoteService(
						QuoteRepositoryPort quoteRepository,
						QuoteCachePort quoteCache,
						PromotionRepositoryPort promotionRepository) {
				this(
								quoteRepository,
								quoteCache,
								promotionRepository,
								new PremiumCalculator(),
								new PromotionDiscountCalculator(),
								new CoverageHealthPolicy(),
								new QuoteStateTransitionService(),
								new CommandValidator(Validation.buildDefaultValidatorFactory().getValidator()));
		}

		public Quote createQuote(CreateQuoteCommand command) {
				commandValidator.validate(command);
				Quote draft =
								Quote.createDraft(
												command.getName(),
												command.getEmail(),
												command.getAge(),
												command.getZipCode());
				return saveWithRecalculatedPremium(draft);
		}

		public Quote updatePersonalInfo(UUID id, UpdatePersonalInfoCommand command) {
				commandValidator.validate(command);
				Quote quote = getQuote(id);
				quoteStateTransitionService.ensureCanUpdatePersonalInfo(quote);
				Quote updated =
								quote.withPersonalInfo(
												command.getName(),
												command.getEmail(),
												command.getAge(),
												command.getZipCode());
				updated = applyAgeAdjustedCoverage(updated, command.getAge());
				return saveWithRecalculatedPremium(updated);
		}

		public Quote updateCoverage(UUID id, UpdateCoverageCommand command) {
				commandValidator.validate(command);
				Quote quote = getQuote(id);
				quoteStateTransitionService.ensureCanUpdateCoverage(quote);
				if (!command.hasAnyField()) {
						return quote;
				}
				coverageHealthPolicy.validateHealthFieldsForPartialUpdate(
								quote.getAge(),
								command.getHasPreexistingConditions(),
								command.getConditions(),
								quote.getHasPreexistingConditions(),
								quote.getConditions());

				CoverageDetails coverageDetails = mergeCoverage(command, quote);
				BigDecimal premium = premiumCalculator.calculate(quote.applyCoverage(coverageDetails));
				Quote updated = quote.applyCoverage(coverageDetails.withPremium(premium));
				return quoteRepository.save(refreshDiscount(updated));
		}

		public Quote updatePromoCode(UUID id, UpdatePromoCommand command) {
				commandValidator.validate(command);
				Quote quote = getQuote(id);
				quoteStateTransitionService.ensureCanUpdatePromoCode(quote);
				ensurePremiumReadyForPromo(quote);

				Promotion promotion =
								promotionRepository
												.findByCode(command.getCode())
												.orElseThrow(
																() ->
																				new QuoteValidationException(
																								QuoteErrorCodes.PROMO_NOT_FOUND,
																								"Promotion code not found: " + command.getCode()));
				if (!promotion.isApplicableAt(Instant.now())) {
						throw new QuoteValidationException(
										QuoteErrorCodes.PROMO_INVALID, "Promotion code is not currently valid");
				}

				BigDecimal discount =
								promotionDiscountCalculator.calculate(
												quote.getEstimatedMonthlyPremium(), promotion.percentage());
				AppliedPromotion applied = AppliedPromotion.from(promotion, discount);
				return quoteRepository.save(quote.applyPromotion(applied));
		}

		public Quote clearPromoCode(UUID id) {
				Quote quote = getQuote(id);
				quoteStateTransitionService.ensureCanUpdatePromoCode(quote);
				if (quote.getAppliedPromotion() == null) {
						return quote;
				}
				return quoteRepository.save(quote.clearPromotion());
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

		private CoverageDetails mergeCoverage(UpdateCoverageCommand command, Quote quote) {
				CoverageType coverageType =
								command.getCoverageType() != null
												? command.getCoverageType()
												: quote.getCoverageType();
				if (coverageType == null) {
						throw new QuoteValidationException(
										"coverageType is required when quote has no coverage set");
				}

				Set<ConditionType> conditions =
								command.getConditions() != null ? command.getConditions() : quote.getConditions();

				return new CoverageDetails(
								coverageType,
								coalesce(
												command.getHasPreexistingConditions(), quote.getHasPreexistingConditions()),
								conditions,
								coalesce(
												command.getTakesPrescriptionMedication(),
												quote.getTakesPrescriptionMedication()),
								coalesce(command.getUsesTobacco(), quote.getUsesTobacco()),
								coalesce(command.getNeedsSpouseCoverage(), quote.getNeedsSpouseCoverage()),
								BigDecimal.ZERO);
		}

		private static <T> T coalesce(T value, T fallback) {
				return value != null ? value : fallback;
		}

		private Quote applyAgeAdjustedCoverage(Quote quote, int age) {
				if (quote.getCoverageType() == null) {
						return quote;
				}

				CoverageDetails coverageDetails =
								new CoverageDetails(
												quote.getCoverageType(),
												quote.getHasPreexistingConditions(),
												quote.getConditions(),
												quote.getTakesPrescriptionMedication(),
												quote.getUsesTobacco(),
												quote.getNeedsSpouseCoverage(),
												BigDecimal.ZERO);
				CoverageDetails adjusted = coverageHealthPolicy.adjustCoverageForAge(coverageDetails, age);
				return adjusted == coverageDetails ? quote : quote.applyCoverage(adjusted);
		}

		private Quote saveWithRecalculatedPremium(Quote quote) {
				if (quote.getCoverageType() == null) {
						return quoteRepository.save(quote);
				}

				CoverageDetails coverageDetails =
								new CoverageDetails(
												quote.getCoverageType(),
												quote.getHasPreexistingConditions(),
												quote.getConditions(),
												quote.getTakesPrescriptionMedication(),
												quote.getUsesTobacco(),
												quote.getNeedsSpouseCoverage(),
												BigDecimal.ZERO);
				BigDecimal premium = premiumCalculator.calculate(quote.applyCoverage(coverageDetails));
				Quote updated = quote.applyCoverage(coverageDetails.withPremium(premium));
				return quoteRepository.save(refreshDiscount(updated));
		}

		private Quote refreshDiscount(Quote quote) {
				if (quote.getAppliedPromotion() == null) {
						return quote;
				}
				BigDecimal discount =
								promotionDiscountCalculator.calculate(
												quote.getEstimatedMonthlyPremium(),
												quote.getAppliedPromotion().percentage());
				return quote.withRecalculatedDiscount(discount);
		}

		private static void ensurePremiumReadyForPromo(Quote quote) {
				BigDecimal premium = quote.getEstimatedMonthlyPremium();
				if (premium == null || premium.compareTo(BigDecimal.ZERO) <= 0) {
						throw new QuoteValidationException(
										QuoteErrorCodes.PROMO_REQUIRES_PREMIUM,
										"A calculated premium is required before applying a promotion code");
				}
		}
}
