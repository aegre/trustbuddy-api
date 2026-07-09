package com.trustbuddy.api.quote.domain.model;

import java.util.Objects;

public record PersonalInfo(String name, String email, int age, String zipCode) {

	public PersonalInfo {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(email, "email");
		Objects.requireNonNull(zipCode, "zipCode");
	}
}
