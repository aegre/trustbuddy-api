package com.trustbuddy.api.quote.infrastructure.cache;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trustbuddy.api.quote.domain.model.Quote;

@Component
public class QuoteCacheMapper {

	private final ObjectMapper objectMapper = createObjectMapper();

	public String toJson(Quote quote) {
		try {
			return objectMapper.writeValueAsString(toDocument(quote));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize quote " + quote.getId(), exception);
		}
	}

	public Quote fromJson(String json) {
		try {
			return toDomain(objectMapper.readValue(json, QuoteCacheDocument.class));
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to deserialize cached quote", exception);
		}
	}

	private static ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}

	private static QuoteCacheDocument toDocument(Quote quote) {
		return new QuoteCacheDocument(
				quote.getId(),
				quote.getName(),
				quote.getEmail(),
				quote.getAge(),
				quote.getZipCode(),
				quote.getCoverageType(),
				quote.getHasPreexistingConditions(),
				quote.getConditions(),
				quote.getTakesPrescriptionMedication(),
				quote.getUsesTobacco(),
				quote.getNeedsSpouseCoverage(),
				quote.getEstimatedMonthlyPremium(),
				quote.getStatus(),
				quote.getCreatedAt(),
				quote.getUpdatedAt(),
				quote.getVersion());
	}

	private static Quote toDomain(QuoteCacheDocument document) {
		return Quote.reconstitute(
				document.id(),
				document.name(),
				document.email(),
				document.age(),
				document.zipCode(),
				document.coverageType(),
				document.hasPreexistingConditions(),
				document.conditions(),
				document.takesPrescriptionMedication(),
				document.usesTobacco(),
				document.needsSpouseCoverage(),
				document.estimatedMonthlyPremium(),
				document.status(),
				document.createdAt(),
				document.updatedAt(),
				document.version());
	}
}
