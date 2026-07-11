package com.trustbuddy.api.quote.application.dto;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import java.util.Collections;
import java.util.Set;

public class UpdateCoverageCommand {

		private CoverageType coverageType;

		private Boolean hasPreexistingConditions;

		private Set<ConditionType> conditions;

		private Boolean takesPrescriptionMedication;

		private Boolean usesTobacco;

		private Boolean needsSpouseCoverage;

		private Boolean hasSpouse;

		public CoverageType getCoverageType() {
				return coverageType;
		}

		public Boolean getHasSpouse() {
				return hasSpouse;
		}

		public void setHasSpouse(Boolean hasSpouse) {
				this.hasSpouse = hasSpouse;
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

		public boolean hasAnyField() {
				return coverageType != null
								|| hasPreexistingConditions != null
								|| conditions != null
								|| takesPrescriptionMedication != null
								|| usesTobacco != null
								|| needsSpouseCoverage != null;
		}
}
