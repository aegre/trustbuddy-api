package com.trustbuddy.api.quote.application.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trustbuddy.api.quote.application.port.out.InsurerGatewayPort;
import com.trustbuddy.api.quote.application.port.out.InsurerSubmissionResult;
import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.exception.ExternalSubmissionException;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.domain.service.CoverageHealthPolicy;
import com.trustbuddy.api.quote.domain.service.QuoteStateTransitionService;

@Service
public class QuoteSubmissionService {

	private final QuoteRepositoryPort quoteRepository;
	private final InsurerGatewayPort insurerGateway;
	private final QuoteStateTransitionService quoteStateTransitionService;
	private final CoverageHealthPolicy coverageHealthPolicy;

	@Autowired
	public QuoteSubmissionService(
			QuoteRepositoryPort quoteRepository,
			InsurerGatewayPort insurerGateway) {
		this(
				quoteRepository,
				insurerGateway,
				new QuoteStateTransitionService(),
				new CoverageHealthPolicy());
	}

	QuoteSubmissionService(
			QuoteRepositoryPort quoteRepository,
			InsurerGatewayPort insurerGateway,
			QuoteStateTransitionService quoteStateTransitionService,
			CoverageHealthPolicy coverageHealthPolicy) {
		this.quoteRepository = quoteRepository;
		this.insurerGateway = insurerGateway;
		this.quoteStateTransitionService = quoteStateTransitionService;
		this.coverageHealthPolicy = coverageHealthPolicy;
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
		if (!quote.hasCoverage()) {
			throw new QuoteValidationException("Quote is missing required coverage data");
		}
		if (quote.getTakesPrescriptionMedication() == null
				|| quote.getUsesTobacco() == null
				|| quote.getNeedsSpouseCoverage() == null) {
			throw new QuoteValidationException("Coverage health answers are required before submit");
		}

		Set<ConditionType> conditions = quote.getConditions().isEmpty() ? null : quote.getConditions();
		coverageHealthPolicy.validateHealthFieldsForAge(
				quote.getAge(),
				quote.getHasPreexistingConditions(),
				conditions);
	}
}
