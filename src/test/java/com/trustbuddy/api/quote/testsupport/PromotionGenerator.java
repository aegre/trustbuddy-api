package com.trustbuddy.api.quote.testsupport;

import com.trustbuddy.api.quote.domain.model.Promotion;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Builds {@link Promotion} instances for tests with sensible defaults. Override only the fields
 * relevant to the scenario under test.
 */
public final class PromotionGenerator {

		private static final String DEFAULT_CODE = "SAVE10";
		private static final BigDecimal DEFAULT_PERCENTAGE = new BigDecimal("10");

		private PromotionGenerator() {}

		public static Promotion active() {
				return builder().build();
		}

		public static Promotion active(String code, BigDecimal percentage) {
				return builder().code(code).percentage(percentage).build();
		}

		public static Builder builder() {
				return new Builder();
		}

		public static final class Builder {

				private UUID id = UUID.randomUUID();
				private String code = DEFAULT_CODE;
				private BigDecimal percentage = DEFAULT_PERCENTAGE;
				private boolean active = true;
				private Instant startsAt = Instant.now().minus(1, ChronoUnit.DAYS);
				private Instant endsAt = Instant.now().plus(30, ChronoUnit.DAYS);

				public Builder id(UUID id) {
						this.id = id;
						return this;
				}

				public Builder code(String code) {
						this.code = code;
						return this;
				}

				public Builder percentage(BigDecimal percentage) {
						this.percentage = percentage;
						return this;
				}

				public Builder active(boolean active) {
						this.active = active;
						return this;
				}

				public Builder startsAt(Instant startsAt) {
						this.startsAt = startsAt;
						return this;
				}

				public Builder endsAt(Instant endsAt) {
						this.endsAt = endsAt;
						return this;
				}

				public Promotion build() {
						return new Promotion(id, code, percentage, active, startsAt, endsAt);
				}
		}
}
