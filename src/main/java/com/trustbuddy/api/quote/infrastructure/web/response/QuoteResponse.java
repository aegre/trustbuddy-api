package com.trustbuddy.api.quote.infrastructure.web.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;

public class QuoteResponse {

	private UUID id;
	private String name;
	private String email;
	private int age;
	private String zipCode;
	private CoverageType coverageType;
	private Boolean hasPreexistingConditions;
	private Set<ConditionType> conditions;
	private Boolean takesPrescriptionMedication;
	private Boolean usesTobacco;
	private Boolean needsSpouseCoverage;
	private BigDecimal estimatedMonthlyPremium;
	private QuoteStatus status;
	private Instant createdAt;
	private Instant updatedAt;
	private long version;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public CoverageType getCoverageType() {
		return coverageType;
	}

	public void setCoverageType(CoverageType coverageType) {
		this.coverageType = coverageType;
	}

	public Boolean getHasPreexistingConditions() {
		return hasPreexistingConditions;
	}

	public void setHasPreexistingConditions(Boolean hasPreexistingConditions) {
		this.hasPreexistingConditions = hasPreexistingConditions;
	}

	public Set<ConditionType> getConditions() {
		return conditions;
	}

	public void setConditions(Set<ConditionType> conditions) {
		this.conditions = conditions;
	}

	public Boolean getTakesPrescriptionMedication() {
		return takesPrescriptionMedication;
	}

	public void setTakesPrescriptionMedication(Boolean takesPrescriptionMedication) {
		this.takesPrescriptionMedication = takesPrescriptionMedication;
	}

	public Boolean getUsesTobacco() {
		return usesTobacco;
	}

	public void setUsesTobacco(Boolean usesTobacco) {
		this.usesTobacco = usesTobacco;
	}

	public Boolean getNeedsSpouseCoverage() {
		return needsSpouseCoverage;
	}

	public void setNeedsSpouseCoverage(Boolean needsSpouseCoverage) {
		this.needsSpouseCoverage = needsSpouseCoverage;
	}

	public BigDecimal getEstimatedMonthlyPremium() {
		return estimatedMonthlyPremium;
	}

	public void setEstimatedMonthlyPremium(BigDecimal estimatedMonthlyPremium) {
		this.estimatedMonthlyPremium = estimatedMonthlyPremium;
	}

	public QuoteStatus getStatus() {
		return status;
	}

	public void setStatus(QuoteStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
