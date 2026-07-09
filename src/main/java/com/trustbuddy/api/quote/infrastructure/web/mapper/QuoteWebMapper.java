package com.trustbuddy.api.quote.infrastructure.web.mapper;

import java.util.LinkedHashSet;

import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.infrastructure.web.response.QuoteResponse;

public final class QuoteWebMapper {

	private QuoteWebMapper() {
	}

	public static QuoteResponse toResponse(Quote quote) {
		QuoteResponse response = new QuoteResponse();
		response.setId(quote.getId());
		response.setName(quote.getName());
		response.setEmail(quote.getEmail());
		response.setAge(quote.getAge());
		response.setZipCode(quote.getZipCode());
		response.setCoverageType(quote.getCoverageType());
		response.setHasPreexistingConditions(quote.getHasPreexistingConditions());
		response.setConditions(new LinkedHashSet<>(quote.getConditions()));
		response.setTakesPrescriptionMedication(quote.getTakesPrescriptionMedication());
		response.setUsesTobacco(quote.getUsesTobacco());
		response.setNeedsSpouseCoverage(quote.getNeedsSpouseCoverage());
		response.setEstimatedMonthlyPremium(quote.getEstimatedMonthlyPremium());
		response.setStatus(quote.getStatus());
		response.setCreatedAt(quote.getCreatedAt());
		response.setUpdatedAt(quote.getUpdatedAt());
		response.setVersion(quote.getVersion());
		return response;
	}
}
