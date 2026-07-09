package com.trustbuddy.api.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Quote {

	private final UUID id;
	private final String name;
	private final String email;
	private final int age;
	private final String zipCode;
	private final CoverageType coverageType;
	private final Boolean hasPreexistingConditions;
	private final Set<ConditionType> conditions;
	private final Boolean takesPrescriptionMedication;
	private final Boolean usesTobacco;
	private final Boolean needsSpouseCoverage;
	private final BigDecimal estimatedMonthlyPremium;
	private final QuoteStatus status;
	private final Instant createdAt;
	private final Instant updatedAt;
	private final long version;

	private Quote(
			UUID id,
			String name,
			String email,
			int age,
			String zipCode,
			CoverageType coverageType,
			Boolean hasPreexistingConditions,
			Set<ConditionType> conditions,
			Boolean takesPrescriptionMedication,
			Boolean usesTobacco,
			Boolean needsSpouseCoverage,
			BigDecimal estimatedMonthlyPremium,
			QuoteStatus status,
			Instant createdAt,
			Instant updatedAt,
			long version) {
		this.id = Objects.requireNonNull(id, "id");
		this.name = Objects.requireNonNull(name, "name");
		this.email = Objects.requireNonNull(email, "email");
		this.age = age;
		this.zipCode = Objects.requireNonNull(zipCode, "zipCode");
		this.coverageType = coverageType;
		this.hasPreexistingConditions = hasPreexistingConditions;
		this.conditions = conditions == null ? Set.of() : Set.copyOf(conditions);
		this.takesPrescriptionMedication = takesPrescriptionMedication;
		this.usesTobacco = usesTobacco;
		this.needsSpouseCoverage = needsSpouseCoverage;
		this.estimatedMonthlyPremium = estimatedMonthlyPremium;
		this.status = Objects.requireNonNull(status, "status");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
		this.version = version;
	}

	public static Quote createDraft(String name, String email, int age, String zipCode) {
		Instant now = Instant.now();
		return new Quote(
				UUID.randomUUID(),
				name,
				email,
				age,
				zipCode,
				null,
				null,
				Set.of(),
				null,
				null,
				null,
				null,
				QuoteStatus.DRAFT,
				now,
				now,
				0L);
	}

	public static Quote reconstitute(
			UUID id,
			String name,
			String email,
			int age,
			String zipCode,
			CoverageType coverageType,
			Boolean hasPreexistingConditions,
			Set<ConditionType> conditions,
			Boolean takesPrescriptionMedication,
			Boolean usesTobacco,
			Boolean needsSpouseCoverage,
			BigDecimal estimatedMonthlyPremium,
			QuoteStatus status,
			Instant createdAt,
			Instant updatedAt,
			long version) {
		return new Quote(
				id,
				name,
				email,
				age,
				zipCode,
				coverageType,
				hasPreexistingConditions,
				conditions,
				takesPrescriptionMedication,
				usesTobacco,
				needsSpouseCoverage,
				estimatedMonthlyPremium,
				status,
				createdAt,
				updatedAt,
				version);
	}

	public Quote applyCoverage(
			CoverageType coverageType,
			Boolean hasPreexistingConditions,
			Set<ConditionType> conditions,
			Boolean takesPrescriptionMedication,
			Boolean usesTobacco,
			Boolean needsSpouseCoverage,
			BigDecimal estimatedMonthlyPremium) {
		return new Quote(
				id,
				name,
				email,
				age,
				zipCode,
				Objects.requireNonNull(coverageType, "coverageType"),
				hasPreexistingConditions,
				conditions,
				takesPrescriptionMedication,
				usesTobacco,
				needsSpouseCoverage,
				Objects.requireNonNull(estimatedMonthlyPremium, "estimatedMonthlyPremium"),
				status,
				createdAt,
				Instant.now(),
				version);
	}

	public Quote withStatus(QuoteStatus status) {
		return new Quote(
				id,
				name,
				email,
				age,
				zipCode,
				coverageType,
				hasPreexistingConditions,
				conditions,
				takesPrescriptionMedication,
				usesTobacco,
				needsSpouseCoverage,
				estimatedMonthlyPremium,
				Objects.requireNonNull(status, "status"),
				createdAt,
				Instant.now(),
				version);
	}

	public Quote withVersion(long version) {
		return new Quote(
				id,
				name,
				email,
				age,
				zipCode,
				coverageType,
				hasPreexistingConditions,
				conditions,
				takesPrescriptionMedication,
				usesTobacco,
				needsSpouseCoverage,
				estimatedMonthlyPremium,
				status,
				createdAt,
				updatedAt,
				version);
	}

	public boolean hasCoverage() {
		return coverageType != null && estimatedMonthlyPremium != null;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public int getAge() {
		return age;
	}

	public String getZipCode() {
		return zipCode;
	}

	public CoverageType getCoverageType() {
		return coverageType;
	}

	public Boolean getHasPreexistingConditions() {
		return hasPreexistingConditions;
	}

	public Set<ConditionType> getConditions() {
		return Collections.unmodifiableSet(conditions);
	}

	public Boolean getTakesPrescriptionMedication() {
		return takesPrescriptionMedication;
	}

	public Boolean getUsesTobacco() {
		return usesTobacco;
	}

	public Boolean getNeedsSpouseCoverage() {
		return needsSpouseCoverage;
	}

	public BigDecimal getEstimatedMonthlyPremium() {
		return estimatedMonthlyPremium;
	}

	public QuoteStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public long getVersion() {
		return version;
	}
}
