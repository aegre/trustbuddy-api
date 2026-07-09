package com.trustbuddy.api.quote.domain.service;

import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;

/**
 * Pure domain state machine for quote lifecycle transitions.
 */
public class QuoteStateTransitionService {

	public void ensureCanUpdateCoverage(Quote quote) {
		if (quote.getStatus() != QuoteStatus.DRAFT) {
			throw new InvalidQuoteStateException(quote.getStatus(), "update coverage");
		}
	}

	public Quote markSubmitted(Quote quote) {
		if (quote.getStatus() == QuoteStatus.SUBMITTED) {
			return quote;
		}
		if (quote.getStatus() == QuoteStatus.DRAFT || quote.getStatus() == QuoteStatus.SUBMISSION_FAILED) {
			return quote.withStatus(QuoteStatus.SUBMITTED);
		}
		throw new InvalidQuoteStateException(quote.getStatus(), "submit");
	}

	public Quote markSubmissionFailed(Quote quote) {
		if (quote.getStatus() == QuoteStatus.SUBMITTED) {
			return quote;
		}
		if (quote.getStatus() == QuoteStatus.DRAFT || quote.getStatus() == QuoteStatus.SUBMISSION_FAILED) {
			return quote.withStatus(QuoteStatus.SUBMISSION_FAILED);
		}
		throw new InvalidQuoteStateException(quote.getStatus(), "mark submission failed");
	}

	public Quote markExpired(Quote quote) {
		if (quote.getStatus() != QuoteStatus.DRAFT) {
			throw new InvalidQuoteStateException(quote.getStatus(), "expire");
		}
		return quote.withStatus(QuoteStatus.EXPIRED);
	}
}
