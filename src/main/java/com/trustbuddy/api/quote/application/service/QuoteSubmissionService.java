package com.trustbuddy.api.quote.application.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trustbuddy.api.quote.application.dto.QuoteSubmissionReadiness;
import com.trustbuddy.api.quote.application.port.out.InsurerGatewayPort;
import com.trustbuddy.api.quote.application.port.out.InsurerSubmissionResult;
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

@Service
public class QuoteSubmissionService {

	private final QuoteRepositoryPort quoteRepository;
	private final InsurerGatewayPort insurerGateway;
	private final QuoteStateTransitionService quoteStateTransitionService;
	private final CoverageHealthPolicy coverageHealthPolicy;
	private final CommandValidator commandValidator;

	@Autowired
	public QuoteSubmissionService(
			QuoteRepositoryPort quoteRepository,
			InsurerGatewayPort insurerGateway,
			CommandValidator commandValidator) {
		this(
				quoteRepository,
				insurerGateway,
				new QuoteStateTransitionService(),
				new CoverageHealthPolicy(),
				commandValidator);
	}

	QuoteSubmissionService(
			QuoteRepositoryPort quoteRepository,
			InsurerGatewayPort insurerGateway,
			QuoteStateTransitionService quoteStateTransitionService,
			CoverageHealthPolicy coverageHealthPolicy,
			CommandValidator commandValidator) {
		this.quoteRepository = quoteRepository;
		this.insurerGateway = insurerGateway;
		this.quoteStateTransitionService = quoteStateTransitionService;
		this.coverageHealthPolicy = coverageHealthPolicy;
		this.commandValidator = commandValidator;
	}

	QuoteSubmissionService(QuoteRepositoryPort quoteRepository, InsurerGatewayPort insurerGateway) {
		this(
				quoteRepository,
				insurerGateway,
				new QuoteStateTransitionService(),
				new CoverageHealthPolicy(),
				new CommandValidator(Validation.buildDefaultValidatorFactory().getValidator()));
	}

	public Quote submitQuote(UUID id) {
		Quote quote = quoteRepository.findById(id)
				.orElseThrow(() -> new QuoteNotFoundException(id));

		if (quote.getStatus() == QuoteStatus.SUBMITTED) {
			return quote;
		}

		if (quote.getStatus() == QuoteStatus.EXPIRED) {
			throw new InvalidQuoteStateException(quote.getStatus(), "submit");
		}

		ensureReadyForSubmission(quote);

		InsurerSubmissionResult result = insurerGateway.submit(quote);
		if (result.success()) {
			return quoteRepository.save(quoteStateTransitionService.markSubmitted(quote));
		}

		quoteRepository.save(quoteStateTransitionService.markSubmissionFailed(quote));
		throw new ExternalSubmissionException(result.message());
	}

	private void ensureReadyForSubmission(Quote quote) {
		commandValidator.validate(QuoteSubmissionReadiness.from(quote));

		Set<ConditionType> conditions = quote.getConditions().isEmpty() ? null : quote.getConditions();
		coverageHealthPolicy.validateHealthFieldsForAge(
				quote.getAge(),
				quote.getHasPreexistingConditions(),
				conditions);
	}
}
