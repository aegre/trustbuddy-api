package com.trustbuddy.api.quote.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QuoteFieldConstraintsTest {

	@Test
	void givenAgeBounds_whenComparedToOpenApiSchemaStrings_thenStayInSync() {
		// When / Then
		assertThat(Integer.parseInt(QuoteFieldConstraints.MIN_AGE_SCHEMA))
				.isEqualTo(QuoteFieldConstraints.MIN_AGE);
		assertThat(Integer.parseInt(QuoteFieldConstraints.MAX_AGE_SCHEMA))
				.isEqualTo(QuoteFieldConstraints.MAX_AGE);
	}
}
