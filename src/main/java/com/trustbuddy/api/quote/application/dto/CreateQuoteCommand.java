package com.trustbuddy.api.quote.application.dto;

import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.MAX_AGE;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.MAX_NAME_LENGTH;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.MIN_AGE;
import static com.trustbuddy.api.quote.application.dto.QuoteFieldConstraints.ZIP_CODE_PATTERN;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateQuoteCommand {

	@NotBlank
	@Size(max = MAX_NAME_LENGTH)
	private String name;

	@NotBlank
	@Email
	@Size(max = MAX_NAME_LENGTH)
	private String email;

	@NotNull
	@Min(MIN_AGE)
	@Max(MAX_AGE)
	private Integer age;

	@NotBlank
	@Pattern(regexp = ZIP_CODE_PATTERN)
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
