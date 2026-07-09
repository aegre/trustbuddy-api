package com.trustbuddy.api.quote.application.dto;

/**
 * Shared field rules for quote personal information.
 *
 * <p>
 * Used by application Bean Validation and web OpenAPI {@code @Schema} metadata so
 * enforcement and API docs stay aligned.
 */
public final class QuoteFieldConstraints {

	public static final int MAX_NAME_LENGTH = 255;

	public static final int MIN_AGE = 1;

	public static final int MAX_AGE = 120;

	public static final String MIN_AGE_SCHEMA = "1";

	public static final String MAX_AGE_SCHEMA = "120";

	public static final String ZIP_CODE_PATTERN = "\\d{5}";

	public static final String ZIP_CODE_DESCRIPTION = "5-digit Mexican postal code (código postal)";

	public static final String ZIP_CODE_EXAMPLE = "06600";

	public static final String NAME_EXAMPLE = "Jane Doe";

	public static final String EMAIL_EXAMPLE = "jane@example.com";

	public static final String AGE_EXAMPLE = "30";

	private QuoteFieldConstraints() {
	}
}
