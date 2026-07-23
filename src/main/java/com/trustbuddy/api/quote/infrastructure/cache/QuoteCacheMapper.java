package com.trustbuddy.api.quote.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trustbuddy.api.quote.domain.model.AppliedPromotion;
import com.trustbuddy.api.quote.domain.model.CoverageDetails;
import com.trustbuddy.api.quote.domain.model.PersonalInfo;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteAudit;
import org.springframework.stereotype.Component;

@Component
public class QuoteCacheMapper {

		private final ObjectMapper objectMapper = createObjectMapper();

		public String toJson(Quote quote) {
				try {
						return objectMapper.writeValueAsString(toDocument(quote));
				} catch (JsonProcessingException exception) {
						throw new IllegalStateException(
										"Failed to serialize quote " + quote.getId(), exception);
				}
		}

		public Quote fromJson(String json) {
				try {
						return toDomain(objectMapper.readValue(json, QuoteCacheDocument.class));
				} catch (JsonProcessingException exception) {
						throw new IllegalStateException("Failed to deserialize cached quote", exception);
				}
		}

		private static ObjectMapper createObjectMapper() {
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
				return mapper;
		}

		private static QuoteCacheDocument toDocument(Quote quote) {
				AppliedPromotion appliedPromotion = quote.getAppliedPromotion();
				return new QuoteCacheDocument(
								quote.getId(),
								quote.getName(),
								quote.getEmail(),
								quote.getAge(),
								quote.getZipCode(),
								quote.getCoverageType(),
								quote.getHasPreexistingConditions(),
								quote.getConditions(),
								quote.getTakesPrescriptionMedication(),
								quote.getUsesTobacco(),
								quote.getNeedsSpouseCoverage(),
								quote.getEstimatedMonthlyPremium(),
								appliedPromotion == null ? null : appliedPromotion.promotionId(),
								appliedPromotion == null ? null : appliedPromotion.code(),
								appliedPromotion == null ? null : appliedPromotion.percentage(),
								appliedPromotion == null ? null : appliedPromotion.discountAmount(),
								quote.getStatus(),
								quote.getCreatedAt(),
								quote.getUpdatedAt(),
								quote.getVersion());
		}

		private static Quote toDomain(QuoteCacheDocument document) {
				CoverageDetails coverage =
								document.coverageType() == null
												? null
												: new CoverageDetails(
																document.coverageType(),
																document.hasPreexistingConditions(),
																document.conditions(),
																document.takesPrescriptionMedication(),
																document.usesTobacco(),
																document.needsSpouseCoverage(),
																document.estimatedMonthlyPremium());
				AppliedPromotion appliedPromotion =
								document.appliedPromotionId() == null
												? null
												: new AppliedPromotion(
																document.appliedPromotionId(),
																document.promoCode(),
																document.promotionPercentage(),
																document.discountAmount());
				return Quote.reconstitute(
								document.id(),
								new PersonalInfo(
												document.name(), document.email(), document.age(), document.zipCode()),
								coverage,
								new QuoteAudit(
												document.status(),
												document.createdAt(),
												document.updatedAt(),
												document.version()),
								appliedPromotion);
		}
}
