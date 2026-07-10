package com.trustbuddy.api.quote.domain.service;

import com.trustbuddy.api.quote.domain.model.Quote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Calculates the estimated monthly premium from coverage and health factors.
 *
 * <p>Formula: {@code basePremium × age × conditions × tobacco × spouse}, rounded {@link
 * RoundingMode#HALF_UP} to two decimal places.
 */
public class PremiumCalculator {

		private static final int PREMIUM_SCALE = 2;

		private final BasePremiumResolver basePremiumResolver;
		private final List<PremiumMultiplier> multipliers;

		public PremiumCalculator() {
				this(
								new BasePremiumResolver(),
								List.of(
												new AgeMultiplier(),
												new ConditionsMultiplier(),
												new TobaccoMultiplier(),
												new SpouseMultiplier()));
		}

		PremiumCalculator(
						BasePremiumResolver basePremiumResolver, List<PremiumMultiplier> multipliers) {
				this.basePremiumResolver =
								Objects.requireNonNull(basePremiumResolver, "basePremiumResolver");
				this.multipliers = List.copyOf(multipliers);
		}

		public BigDecimal calculate(Quote quote) {
				Objects.requireNonNull(quote, "quote must not be null");
				var coverageType =
								Objects.requireNonNull(quote.getCoverageType(), "coverageType must not be null");

				BigDecimal premium = basePremiumResolver.resolve(coverageType);
				for (PremiumMultiplier multiplier : multipliers) {
						premium = premium.multiply(multiplier.multiplierFor(quote));
				}
				return premium.setScale(PREMIUM_SCALE, RoundingMode.HALF_UP);
		}
}
