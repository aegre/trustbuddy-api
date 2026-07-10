package com.trustbuddy.api.quote.infrastructure.web.request;

import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.AGE_EXAMPLE;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.EMAIL_EXAMPLE;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.MAX_AGE_SCHEMA;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.MAX_NAME_LENGTH;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.MIN_AGE_SCHEMA;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.NAME_EXAMPLE;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.ZIP_CODE_DESCRIPTION;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.ZIP_CODE_EXAMPLE;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.ZIP_CODE_PATTERN;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Personal information for creating a draft quote")
public class CreateQuoteRequest {

		@Schema(
						description = "Applicant full name",
						example = NAME_EXAMPLE,
						requiredMode = Schema.RequiredMode.REQUIRED,
						maxLength = MAX_NAME_LENGTH)
		private String name;

		@Schema(
						description = "Applicant email address",
						example = EMAIL_EXAMPLE,
						requiredMode = Schema.RequiredMode.REQUIRED,
						format = "email",
						maxLength = MAX_NAME_LENGTH)
		private String email;

		@Schema(
						description = "Applicant age in years",
						example = AGE_EXAMPLE,
						requiredMode = Schema.RequiredMode.REQUIRED,
						minimum = MIN_AGE_SCHEMA,
						maximum = MAX_AGE_SCHEMA)
		private Integer age;

		@Schema(
						description = ZIP_CODE_DESCRIPTION,
						example = ZIP_CODE_EXAMPLE,
						requiredMode = Schema.RequiredMode.REQUIRED,
						pattern = ZIP_CODE_PATTERN)
		private String zipCode;

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

		public Integer getAge() {
				return age;
		}

		public void setAge(Integer age) {
				this.age = age;
		}

		public String getZipCode() {
				return zipCode;
		}

		public void setZipCode(String zipCode) {
				this.zipCode = zipCode;
		}
}
