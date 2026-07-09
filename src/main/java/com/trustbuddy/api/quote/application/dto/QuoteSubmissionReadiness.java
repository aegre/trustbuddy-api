package com.trustbuddy.api.quote.application.dto;

import static com.trustbuddy.api.quote.application.validation.SubmissionValidationGroups.Coverage;
import static com.trustbuddy.api.quote.application.validation.SubmissionValidationGroups.CoverageAnswers;
import static com.trustbuddy.api.quote.application.validation.SubmissionValidationGroups.PersonalInfo;

import java.math.BigDecimal;

import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.Quote;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@GroupSequence({
		QuoteSubmissionReadiness.class,
		PersonalInfo.class,
		Coverage.class,
		CoverageAnswers.class
})
public class QuoteSubmissionReadiness {

	@NotBlank(message = "name is required", groups = PersonalInfo.class)
	private String name;

	@NotBlank(message = "email is required", groups = PersonalInfo.class)
	private String email;

	@Min(value = 1, message = "age is required", groups = PersonalInfo.class)
	private int age;

	@NotBlank(message = "zipCode is required", groups = PersonalInfo.class)
	private String zipCode;

	@NotNull(message = "Quote is missing required coverage data", groups = Coverage.class)
	private CoverageType coverageType;

	@NotNull(message = "Quote is missing required coverage data", groups = Coverage.class)
	private BigDecimal estimatedMonthlyPremium;

	@NotNull(message = "takesPrescriptionMedication is required", groups = CoverageAnswers.class)
	private Boolean takesPrescriptionMedication;

	@NotNull(message = "usesTobacco is required", groups = CoverageAnswers.class)
	private Boolean usesTobacco;

	@NotNull(message = "needsSpouseCoverage is required", groups = CoverageAnswers.class)
	private Boolean needsSpouseCoverage;

	public static QuoteSubmissionReadiness from(Quote quote) {
		QuoteSubmissionReadiness readiness = new QuoteSubmissionReadiness();
		readiness.setName(quote.getName());
		readiness.setEmail(quote.getEmail());
		readiness.setAge(quote.getAge());
		readiness.setZipCode(quote.getZipCode());
		readiness.setCoverageType(quote.getCoverageType());
		readiness.setEstimatedMonthlyPremium(quote.getEstimatedMonthlyPremium());
		readiness.setTakesPrescriptionMedication(quote.getTakesPrescriptionMedication());
		readiness.setUsesTobacco(quote.getUsesTobacco());
		readiness.setNeedsSpouseCoverage(quote.getNeedsSpouseCoverage());
		return readiness;
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

	public BigDecimal getEstimatedMonthlyPremium() {
		return estimatedMonthlyPremium;
	}

	public void setEstimatedMonthlyPremium(BigDecimal estimatedMonthlyPremium) {
		this.estimatedMonthlyPremium = estimatedMonthlyPremium;
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
