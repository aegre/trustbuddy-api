package com.trustbuddy.api.quote.application.service;

import com.trustbuddy.api.config.QuoteMetrics;
import com.trustbuddy.api.quote.application.dto.QuoteSubmissionReadiness;
import com.trustbuddy.api.quote.application.port.out.InsurerGatewayPort;
import com.trustbuddy.api.quote.application.port.out.InsurerSubmissionResult;
import com.trustbuddy.api.quote.application.port.out.QuoteEventPublisherPort;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.application.validation.CommandValidator;
import com.trustbuddy.api.quote.domain.exception.ExternalSubmissionException;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.domain.service.CoverageHealthPolicy;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;
import jakarta.validation.Validation;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuoteSubmissionService {

		private final QuoteRepositoryPort quoteRepository;
		private final InsurerGatewayPort insurerGateway;
		private final QuoteEventPublisherPort quoteEventPublisher;
		private final QuoteStateTransitionService quoteStateTransitionService;
		private final CoverageHealthPolicy coverageHealthPolicy;
		private final CommandValidator commandValidator;
		private final QuoteMetrics quoteMetrics;

		@Autowired
		public QuoteSubmissionService(
						QuoteRepositoryPort quoteRepository,
						InsurerGatewayPort insurerGateway,
						QuoteEventPublisherPort quoteEventPublisher,
						CommandValidator commandValidator,
						QuoteMetrics quoteMetrics) {
				this(
								quoteRepository,
								insurerGateway,
								quoteEventPublisher,
								new QuoteStateTransitionService(),
								new CoverageHealthPolicy(),
								commandValidator,
								quoteMetrics);
		}

		QuoteSubmissionService(
						QuoteRepositoryPort quoteRepository,
						InsurerGatewayPort insurerGateway,
						QuoteEventPublisherPort quoteEventPublisher,
						QuoteStateTransitionService quoteStateTransitionService,
						CoverageHealthPolicy coverageHealthPolicy,
						CommandValidator commandValidator,
						QuoteMetrics quoteMetrics) {
				this.quoteRepository = quoteRepository;
				this.insurerGateway = insurerGateway;
				this.quoteEventPublisher = quoteEventPublisher;
				this.quoteStateTransitionService = quoteStateTransitionService;
				this.coverageHealthPolicy = coverageHealthPolicy;
				this.commandValidator = commandValidator;
				this.quoteMetrics = quoteMetrics;
		}

		QuoteSubmissionService(
						QuoteRepositoryPort quoteRepository,
						InsurerGatewayPort insurerGateway,
						QuoteEventPublisherPort quoteEventPublisher) {
				this(
								quoteRepository,
								insurerGateway,
								quoteEventPublisher,
								new QuoteStateTransitionService(),
								new CoverageHealthPolicy(),
								new CommandValidator(Validation.buildDefaultValidatorFactory().getValidator()),
								QuoteMetrics.noop());
		}

		public Quote submitQuote(UUID id) {
				Quote quote =
								quoteRepository.findById(id).orElseThrow(() -> new QuoteNotFoundException(id));

				if (quote.getStatus() == QuoteStatus.SUBMITTED) {
						return quote;
				}

				if (quote.getStatus() == QuoteStatus.EXPIRED) {
						throw new InvalidQuoteStateException(quote.getStatus(), "submit");
				}

				ensureReadyForSubmission(quote);

				InsurerSubmissionResult result = insurerGateway.submit(quote);
				if (result.success()) {
						Quote submitted =
										quoteRepository.save(quoteStateTransitionService.markSubmitted(quote));
						quoteEventPublisher.publishQuoteSubmitted(submitted);
						quoteMetrics.recordSubmission();
						return submitted;
				}

				quoteRepository.save(quoteStateTransitionService.markSubmissionFailed(quote));
				quoteMetrics.recordSubmissionFailed();
				throw new ExternalSubmissionException(result.message());
		}

		private void ensureReadyForSubmission(Quote quote) {
				commandValidator.validate(QuoteSubmissionReadiness.from(quote));

				Set<ConditionType> conditions =
								quote.getConditions().isEmpty() ? null : quote.getConditions();
				coverageHealthPolicy.validateHealthFieldsForAge(
								quote.getAge(), quote.getHasPreexistingConditions(), conditions);
		}
}
