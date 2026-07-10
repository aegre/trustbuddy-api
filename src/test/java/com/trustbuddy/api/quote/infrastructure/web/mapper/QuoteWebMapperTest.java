package com.trustbuddy.api.quote.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.web.response.QuoteResponse;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class QuoteWebMapperTest {

		@Test
		void givenDraftQuote_whenToResponse_thenMapsPersonalFieldsOnly() {
				// Given
				Quote quote = QuoteGenerator.draft(30);

				// When
				QuoteResponse response = QuoteWebMapper.toResponse(quote);

				// Then
				assertThat(response.getId()).isEqualTo(quote.getId());
				assertThat(response.getName()).isEqualTo("Jane Doe");
				assertThat(response.getEmail()).isEqualTo("jane@example.com");
				assertThat(response.getAge()).isEqualTo(30);
				assertThat(response.getZipCode()).isEqualTo(QuoteFieldConstraints.ZIP_CODE_EXAMPLE);
				assertThat(response.getStatus()).isEqualTo(QuoteStatus.DRAFT);
				assertThat(response.getCoverageType()).isNull();
				assertThat(response.getEstimatedMonthlyPremium()).isNull();
		}

		@Test
		void givenQuoteWithCoverage_whenToResponse_thenMapsCoverageAndHealthFields() {
				// Given
				Quote quote =
								QuoteGenerator.coverage(70, CoverageType.STANDARD)
												.hasPreexistingConditions(true)
												.conditions(ConditionType.DIABETES)
												.usesTobacco(true)
												.needsSpouseCoverage(true)
												.build();

				// When
				QuoteResponse response = QuoteWebMapper.toResponse(quote);

				// Then
				assertThat(response.getCoverageType()).isEqualTo(CoverageType.STANDARD);
				assertThat(response.getHasPreexistingConditions()).isTrue();
				assertThat(response.getConditions()).containsExactly(ConditionType.DIABETES);
				assertThat(response.getUsesTobacco()).isTrue();
				assertThat(response.getNeedsSpouseCoverage()).isTrue();
				assertThat(response.getEstimatedMonthlyPremium()).isEqualByComparingTo(BigDecimal.ZERO);
		}
}
