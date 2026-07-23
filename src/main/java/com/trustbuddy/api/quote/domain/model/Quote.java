package com.trustbuddy.api.quote.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Quote {

		private final UUID id;
		private final PersonalInfo personalInfo;
		private final CoverageDetails coverage;
		private final QuoteAudit audit;
		private final AppliedPromotion appliedPromotion;

		private Quote(
						UUID id,
						PersonalInfo personalInfo,
						CoverageDetails coverage,
						QuoteAudit audit,
						AppliedPromotion appliedPromotion) {
				this.id = Objects.requireNonNull(id, "id");
				this.personalInfo = Objects.requireNonNull(personalInfo, "personalInfo");
				this.coverage = coverage;
				this.audit = Objects.requireNonNull(audit, "audit");
				this.appliedPromotion = appliedPromotion;
		}

		public static Quote createDraft(String name, String email, int age, String zipCode) {
				Instant now = Instant.now();
				return new Quote(
								UUID.randomUUID(),
								new PersonalInfo(name, email, age, zipCode),
								CoverageDetails.initialStandard(),
								new QuoteAudit(QuoteStatus.DRAFT, now, now, 0L),
								null);
		}

		public static Quote reconstitute(
						UUID id,
						PersonalInfo personalInfo,
						CoverageDetails coverage,
						QuoteAudit audit,
						AppliedPromotion appliedPromotion) {
				return new Quote(id, personalInfo, coverage, audit, appliedPromotion);
		}

		public Quote applyCoverage(CoverageDetails coverage) {
				return new Quote(
								id,
								personalInfo,
								Objects.requireNonNull(coverage, "coverage"),
								audit.touch(),
								appliedPromotion);
		}

		public Quote withStatus(QuoteStatus status) {
				return new Quote(id, personalInfo, coverage, audit.withStatus(status), appliedPromotion);
		}

		public Quote withVersion(long version) {
				return new Quote(id, personalInfo, coverage, audit.withVersion(version), appliedPromotion);
		}

		public Quote withPersonalInfo(String name, String email, int age, String zipCode) {
				return new Quote(
								id,
								new PersonalInfo(name, email, age, zipCode),
								coverage,
								audit.touch(),
								appliedPromotion);
		}

		public Quote applyPromotion(AppliedPromotion promotion) {
				return new Quote(
								id,
								personalInfo,
								coverage,
								audit.touch(),
								Objects.requireNonNull(promotion, "appliedPromotion"));
		}

		public Quote clearPromotion() {
				return new Quote(id, personalInfo, coverage, audit.touch(), null);
		}

		public Quote withRecalculatedDiscount(BigDecimal discountAmount) {
				if (appliedPromotion == null) {
						return this;
				}
				return new Quote(
								id,
								personalInfo,
								coverage,
								audit.touch(),
								appliedPromotion.withDiscountAmount(discountAmount));
		}

		public boolean hasCoverage() {
				return coverage != null && coverage.estimatedMonthlyPremium() != null;
		}

		public UUID getId() {
				return id;
		}

		public String getName() {
				return personalInfo.name();
		}

		public String getEmail() {
				return personalInfo.email();
		}

		public int getAge() {
				return personalInfo.age();
		}

		public String getZipCode() {
				return personalInfo.zipCode();
		}

		public CoverageType getCoverageType() {
				return coverage == null ? null : coverage.coverageType();
		}

		public Boolean getHasPreexistingConditions() {
				return coverage == null ? null : coverage.hasPreexistingConditions();
		}

		public Set<ConditionType> getConditions() {
				return coverage == null ? Set.of() : Collections.unmodifiableSet(coverage.conditions());
		}

		public Boolean getTakesPrescriptionMedication() {
				return coverage == null ? null : coverage.takesPrescriptionMedication();
		}

		public Boolean getUsesTobacco() {
				return coverage == null ? null : coverage.usesTobacco();
		}

		public Boolean getNeedsSpouseCoverage() {
				return coverage == null ? null : coverage.needsSpouseCoverage();
		}

		public BigDecimal getEstimatedMonthlyPremium() {
				return coverage == null ? null : coverage.estimatedMonthlyPremium();
		}

		public AppliedPromotion getAppliedPromotion() {
				return appliedPromotion;
		}

		public QuoteStatus getStatus() {
				return audit.status();
		}

		public Instant getCreatedAt() {
				return audit.createdAt();
		}

		public Instant getUpdatedAt() {
				return audit.updatedAt();
		}

		public long getVersion() {
				return audit.version();
		}
}
