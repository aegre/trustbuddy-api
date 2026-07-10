package com.trustbuddy.api.quote.infrastructure.persistence.entity;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "quotes")
public class QuoteEntity {

		@Id private UUID id;

		@Column(nullable = false)
		private String name;

		@Column(nullable = false)
		private String email;

		@Column(nullable = false)
		private int age;

		@Column(nullable = false, length = 20)
		private String zipCode;

		@Enumerated(EnumType.STRING)
		private CoverageType coverageType;

		private Boolean hasPreexistingConditions;

		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "quote_conditions", joinColumns = @JoinColumn(name = "quote_id"))
		@Enumerated(EnumType.STRING)
		@Column(name = "condition_type", nullable = false)
		private Set<ConditionType> conditions = new HashSet<>();

		private Boolean takesPrescriptionMedication;

		private Boolean usesTobacco;

		private Boolean needsSpouseCoverage;

		@Column(precision = 19, scale = 2)
		private BigDecimal estimatedMonthlyPremium;

		@Enumerated(EnumType.STRING)
		@Column(nullable = false)
		private QuoteStatus status;

		@Column(nullable = false)
		private Instant createdAt;

		@Column(nullable = false)
		private Instant updatedAt;

		@Version private long version;

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
