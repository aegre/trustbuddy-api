package com.trustbuddy.api.quote.infrastructure.web.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Personal information for creating a draft quote")
public class CreateQuoteRequest {

	@Schema(
			description = "Applicant full name",
			example = "Jane Doe",
			requiredMode = Schema.RequiredMode.REQUIRED,
			maxLength = 255)
	private String name;

	@Schema(
			description = "Applicant email address",
			example = "jane@example.com",
			requiredMode = Schema.RequiredMode.REQUIRED,
			format = "email",
			maxLength = 255)
	private String email;

	@Schema(
			description = "Applicant age in years",
			example = "30",
			requiredMode = Schema.RequiredMode.REQUIRED,
			minimum = "1",
			maximum = "120")
	private Integer age;

	@Schema(
			description = "5-digit US zip code",
			example = "12345",
			requiredMode = Schema.RequiredMode.REQUIRED,
			pattern = "\\d{5}")
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
