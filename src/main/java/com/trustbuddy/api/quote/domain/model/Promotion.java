package com.trustbuddy.api.quote.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Catalog promotion looked up by normalized code. Validity is evaluated at apply time via {@link
 * #isApplicableAt(Instant)}.
 */
public record Promotion(
				UUID id,
				String code,
				BigDecimal percentage,
				boolean active,
				Instant startsAt,
				Instant endsAt) {

		public Promotion {
				Objects.requireNonNull(id, "id");
				Objects.requireNonNull(code, "code");
				Objects.requireNonNull(percentage, "percentage");
				Objects.requireNonNull(startsAt, "startsAt");
				Objects.requireNonNull(endsAt, "endsAt");
				code = normalizeCode(code);
				if (percentage.compareTo(BigDecimal.ZERO) <= 0
								|| percentage.compareTo(new BigDecimal("100")) > 0) {
						throw new IllegalArgumentException("percentage must be greater than 0 and at most 100");
				}
				if (endsAt.isBefore(startsAt)) {
						throw new IllegalArgumentException("endsAt must not be before startsAt");
				}
		}

		public static String normalizeCode(String code) {
				Objects.requireNonNull(code, "code");
				return code.trim().toUpperCase(Locale.ENGLISH);
		}

		/** True when the promotion is active and {@code at} falls within {@code [startsAt, endsAt]}. */
		public boolean isApplicableAt(Instant at) {
				Objects.requireNonNull(at, "at");
				return active && !at.isBefore(startsAt) && !at.isAfter(endsAt);
		}
}
