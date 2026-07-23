package com.trustbuddy.api.quote.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Snapshot of a catalog {@link Promotion} applied to a quote. Stores the percentage at apply time
 * so later catalog edits do not rewrite historical quotes; {@code discountAmount} is refreshed when
 * premium recalculates.
 */
public record AppliedPromotion(
				UUID promotionId, String code, BigDecimal percentage, BigDecimal discountAmount) {

		public AppliedPromotion {
				Objects.requireNonNull(promotionId, "promotionId");
				Objects.requireNonNull(code, "code");
				Objects.requireNonNull(percentage, "percentage");
				Objects.requireNonNull(discountAmount, "discountAmount");
				code = Promotion.normalizeCode(code);
		}

		public AppliedPromotion withDiscountAmount(BigDecimal newDiscountAmount) {
				return new AppliedPromotion(
								promotionId,
								code,
								percentage,
								Objects.requireNonNull(newDiscountAmount, "discountAmount"));
		}

		public static AppliedPromotion from(Promotion promotion, BigDecimal discountAmount) {
				Objects.requireNonNull(promotion, "promotion");
				return new AppliedPromotion(
								promotion.id(), promotion.code(), promotion.percentage(), discountAmount);
		}
}
