package com.trustbuddy.api.quote.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints;
import com.trustbuddy.api.quote.domain.model.AppliedPromotion;
import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageDetails;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.web.response.QuoteResponse;
import com.trustbuddy.api.quote.testsupport.PromotionGenerator;
import com.trustbuddy.api.quote.testsupport.QuoteGenerator;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuoteWebMapperTest {

		@Test
		void givenDraftQuote_whenToResponse_thenMapsPersonalAndDefaultCoverageFields() {
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
				assertThat(response.getCoverageType()).isEqualTo(CoverageType.STANDARD);
				assertThat(response.getEstimatedMonthlyPremium()).isEqualByComparingTo(BigDecimal.ZERO);
				assertThat(response.getPromoCode()).isNull();
				assertThat(response.getPromotionPercentage()).isNull();
				assertThat(response.getDiscountAmount()).isNull();
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

		@Test
		void givenQuoteWithAppliedPromotion_whenToResponse_thenMapsPromoFields() {
				// Given
				var promotion = PromotionGenerator.active("SAVE10", new BigDecimal("10"));
				Quote quote =
								QuoteGenerator.coverage(30, CoverageType.STANDARD)
												.takesPrescriptionMedication(false)
												.usesTobacco(false)
												.needsSpouseCoverage(false)
												.build()
												.applyCoverage(
																new CoverageDetails(
																				CoverageType.STANDARD,
																				null,
																				Set.of(),
																				false,
																				false,
																				false,
																				new BigDecimal("100.00")))
												.applyPromotion(AppliedPromotion.from(promotion, new BigDecimal("10.00")));

				// When
				QuoteResponse response = QuoteWebMapper.toResponse(quote);

				// Then
				assertThat(response.getEstimatedMonthlyPremium()).isEqualByComparingTo("100.00");
				assertThat(response.getPromoCode()).isEqualTo("SAVE10");
				assertThat(response.getPromotionPercentage()).isEqualByComparingTo("10");
				assertThat(response.getDiscountAmount()).isEqualByComparingTo("10.00");
		}
}
