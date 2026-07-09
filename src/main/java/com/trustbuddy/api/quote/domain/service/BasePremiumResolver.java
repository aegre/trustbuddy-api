package com.trustbuddy.api.quote.domain.service;

import java.math.BigDecimal;
import java.util.Objects;

import com.trustbuddy.api.quote.domain.model.CoverageType;

/**
 * Resolves the base monthly premium for a coverage type.
 */
public class BasePremiumResolver {

	private static final BigDecimal BASIC_BASE = new BigDecimal("50");
	private static final BigDecimal STANDARD_BASE = new BigDecimal("100");
	private static final BigDecimal PREMIUM_BASE = new BigDecimal("200");

	public BigDecimal resolve(CoverageType coverageType) {
		Objects.requireNonNull(coverageType, "coverageType");
		return switch (coverageType) {
			case BASIC -> BASIC_BASE;
			case STANDARD -> STANDARD_BASE;
			case PREMIUM -> PREMIUM_BASE;
		};
	}
}
