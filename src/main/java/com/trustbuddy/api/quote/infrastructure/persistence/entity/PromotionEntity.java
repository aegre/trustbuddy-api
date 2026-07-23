package com.trustbuddy.api.quote.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA mapping for the {@code promotions} catalog table.
 *
 * <p>Columns (created via {@code ddl-auto=update} in {@code dev}/{@code docker} profiles; validated
 * elsewhere — Flyway migrations not introduced yet):
 *
 * <ul>
 *   <li>{@code id} UUID PK
 *   <li>{@code code} VARCHAR(64) unique, stored normalized (trim + uppercase)
 *   <li>{@code percentage} NUMERIC(5,2) — percent off premium (e.g. 10.00 = 10%)
 *   <li>{@code active} BOOLEAN
 *   <li>{@code starts_at} TIMESTAMPTZ
 *   <li>{@code ends_at} TIMESTAMPTZ
 * </ul>
 */
@Entity
@Table(name = "promotions")
public class PromotionEntity {

		@Id private UUID id;

		@Column(nullable = false, unique = true, length = 64)
		private String code;

		@Column(nullable = false, precision = 5, scale = 2)
		private BigDecimal percentage;

		@Column(nullable = false)
		private boolean active;

		@Column(name = "starts_at", nullable = false)
		private Instant startsAt;

		@Column(name = "ends_at", nullable = false)
		private Instant endsAt;

		public UUID getId() {
				return id;
		}

		public void setId(UUID id) {
				this.id = id;
		}

		public String getCode() {
				return code;
		}

		public void setCode(String code) {
				this.code = code;
		}

		public BigDecimal getPercentage() {
				return percentage;
		}

		public void setPercentage(BigDecimal percentage) {
				this.percentage = percentage;
		}

		public boolean isActive() {
				return active;
		}

		public void setActive(boolean active) {
				this.active = active;
		}

		public Instant getStartsAt() {
				return startsAt;
		}

		public void setStartsAt(Instant startsAt) {
				this.startsAt = startsAt;
		}

		public Instant getEndsAt() {
				return endsAt;
		}

		public void setEndsAt(Instant endsAt) {
				this.endsAt = endsAt;
		}
}
