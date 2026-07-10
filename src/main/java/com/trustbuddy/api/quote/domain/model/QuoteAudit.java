package com.trustbuddy.api.quote.domain.model;

import java.time.Instant;
import java.util.Objects;

public record QuoteAudit(QuoteStatus status, Instant createdAt, Instant updatedAt, long version) {

		public QuoteAudit {
				Objects.requireNonNull(status, "status");
				Objects.requireNonNull(createdAt, "createdAt");
				Objects.requireNonNull(updatedAt, "updatedAt");
		}

		public QuoteAudit touch() {
				return new QuoteAudit(status, createdAt, Instant.now(), version);
		}

		public QuoteAudit withStatus(QuoteStatus newStatus) {
				return new QuoteAudit(
								Objects.requireNonNull(newStatus, "status"), createdAt, Instant.now(), version);
		}

		public QuoteAudit withVersion(long newVersion) {
				return new QuoteAudit(status, createdAt, updatedAt, newVersion);
		}
}
