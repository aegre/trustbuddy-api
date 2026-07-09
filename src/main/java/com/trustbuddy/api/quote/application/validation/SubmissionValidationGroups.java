package com.trustbuddy.api.quote.application.validation;

/**
 * Ordered validation groups for {@link com.trustbuddy.api.quote.application.dto.QuoteSubmissionReadiness}.
 *
 * <p>
 * Groups run in sequence so coverage and answer checks are not reported before
 * personal information and coverage completeness.
 */
public final class SubmissionValidationGroups {

	public interface PersonalInfo {
	}

	public interface Coverage {
	}

	public interface CoverageAnswers {
	}

	private SubmissionValidationGroups() {
	}
}
