package com.trustbuddy.api.quote.infrastructure.persistence.mapper;

import com.trustbuddy.api.quote.domain.model.AppliedPromotion;
import com.trustbuddy.api.quote.domain.model.CoverageDetails;
import com.trustbuddy.api.quote.domain.model.PersonalInfo;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteAudit;
import com.trustbuddy.api.quote.infrastructure.persistence.entity.QuoteEntity;
import java.util.HashSet;
import org.springframework.stereotype.Component;

@Component
public class QuotePersistenceMapper {

		public void updateEntity(Quote quote, QuoteEntity entity) {
				entity.setId(quote.getId());
				entity.setName(quote.getName());
				entity.setEmail(quote.getEmail());
				entity.setAge(quote.getAge());
				entity.setZipCode(quote.getZipCode());
				entity.setCoverageType(quote.getCoverageType());
				entity.setHasPreexistingConditions(quote.getHasPreexistingConditions());
				entity.setConditions(new HashSet<>(quote.getConditions()));
				entity.setTakesPrescriptionMedication(quote.getTakesPrescriptionMedication());
				entity.setUsesTobacco(quote.getUsesTobacco());
				entity.setNeedsSpouseCoverage(quote.getNeedsSpouseCoverage());
				entity.setEstimatedMonthlyPremium(quote.getEstimatedMonthlyPremium());
				AppliedPromotion appliedPromotion = quote.getAppliedPromotion();
				if (appliedPromotion == null) {
						entity.setAppliedPromotionId(null);
						entity.setPromoCode(null);
						entity.setPromotionPercentage(null);
						entity.setDiscountAmount(null);
				} else {
						entity.setAppliedPromotionId(appliedPromotion.promotionId());
						entity.setPromoCode(appliedPromotion.code());
						entity.setPromotionPercentage(appliedPromotion.percentage());
						entity.setDiscountAmount(appliedPromotion.discountAmount());
				}
				entity.setStatus(quote.getStatus());
				entity.setCreatedAt(quote.getCreatedAt());
				entity.setUpdatedAt(quote.getUpdatedAt());
				entity.setVersion(quote.getVersion());
		}

		public Quote toDomain(QuoteEntity entity) {
				return Quote.reconstitute(
								entity.getId(),
								new PersonalInfo(
												entity.getName(), entity.getEmail(), entity.getAge(), entity.getZipCode()),
								toCoverageDetails(entity),
								new QuoteAudit(
												entity.getStatus(),
												entity.getCreatedAt(),
												entity.getUpdatedAt(),
												entity.getVersion()),
								toAppliedPromotion(entity));
		}

		private CoverageDetails toCoverageDetails(QuoteEntity entity) {
				if (entity.getCoverageType() == null) {
						return null;
				}
				return new CoverageDetails(
								entity.getCoverageType(),
								entity.getHasPreexistingConditions(),
								entity.getConditions(),
								entity.getTakesPrescriptionMedication(),
								entity.getUsesTobacco(),
								entity.getNeedsSpouseCoverage(),
								entity.getEstimatedMonthlyPremium());
		}

		private AppliedPromotion toAppliedPromotion(QuoteEntity entity) {
				if (entity.getAppliedPromotionId() == null) {
						return null;
				}
				return new AppliedPromotion(
								entity.getAppliedPromotionId(),
								entity.getPromoCode(),
								entity.getPromotionPercentage(),
								entity.getDiscountAmount());
		}
}
