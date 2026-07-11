package com.trustbuddy.api.quote.infrastructure.web.request;

import com.trustbuddy.api.quote.domain.model.ConditionType;
import com.trustbuddy.api.quote.domain.model.CoverageType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.Set;

@Schema(description = "Partial coverage update; omit fields to leave them unchanged")
public class UpdateCoverageRequest {

		@Schema(description = "Selected coverage tier", example = "STANDARD")
		private CoverageType coverageType;

		@Schema(
						description =
										"Whether the applicant has pre-existing conditions (required when age is over 65)")
		private Boolean hasPreexistingConditions;

		@Schema(
						description =
										"Reported pre-existing conditions (required when hasPreexistingConditions is true)")
		private Set<ConditionType> conditions;

		@Schema(description = "Whether the applicant takes prescription medication")
		private Boolean takesPrescriptionMedication;

		@Schema(description = "Whether the applicant uses tobacco")
		private Boolean usesTobacco;

		@Schema(description = "Whether spouse coverage is needed")
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
