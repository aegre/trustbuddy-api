package com.trustbuddy.api.quote.infrastructure.web.request;

import java.util.Collections;
import java.util.Set;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;

import jakarta.validation.constraints.NotNull;

public class UpdateCoverageRequest {

	@NotNull
	private CoverageType coverageType;

	private Boolean hasPreexistingConditions;

	private Set<ConditionType> conditions;

	private Boolean takesPrescriptionMedication;

	private Boolean usesTobacco;

	private Boolean needsSpouseCoverage;

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
		return conditions == null ? null : Collections.unmodifiableSet(conditions);
	}

	public void setConditions(Set<ConditionType> conditions) {
		this.conditions = conditions == null ? null : Set.copyOf(conditions);
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
}
