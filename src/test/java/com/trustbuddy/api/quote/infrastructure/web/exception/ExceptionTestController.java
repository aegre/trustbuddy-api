package com.trustbuddy.api.quote.infrastructure.web.exception;

import com.trustbuddy.api.quote.domain.exception.ConditionalFieldRejectedException;
import com.trustbuddy.api.quote.domain.exception.ExternalSubmissionException;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/exceptions")
class ExceptionTestController {

		@GetMapping("/quote-not-found")
		void quoteNotFound() {
				throw new QuoteNotFoundException(UUID.fromString("00000000-0000-0000-0000-000000000001"));
		}

		@GetMapping("/invalid-state")
		void invalidState() {
				throw new InvalidQuoteStateException(QuoteStatus.SUBMITTED, "update coverage");
		}

		@GetMapping("/conditional-field")
		void conditionalField() {
				throw new ConditionalFieldRejectedException(
								"Supplemental health fields are not allowed when age is 65 or younger");
		}

		@GetMapping("/quote-validation")
		void quoteValidation() {
				throw new QuoteValidationException(
								"hasPreexistingConditions is required when age is over 65");
		}

		@GetMapping("/external-submission")
		void externalSubmission() {
				throw new ExternalSubmissionException("Insurer gateway returned an error");
		}
}
