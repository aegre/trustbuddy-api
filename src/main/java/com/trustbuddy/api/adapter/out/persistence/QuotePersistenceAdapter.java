package com.trustbuddy.api.adapter.out.persistence;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.trustbuddy.api.adapter.out.persistence.entity.QuoteEntity;
import com.trustbuddy.api.adapter.out.persistence.repository.QuoteJpaRepository;
import com.trustbuddy.api.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.domain.model.Quote;
import com.trustbuddy.api.domain.model.QuoteStatus;

@Component
public class QuotePersistenceAdapter implements QuoteRepositoryPort {

	private final QuoteJpaRepository jpaRepository;

	public QuotePersistenceAdapter(QuoteJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Quote save(Quote quote) {
		QuoteEntity entity = jpaRepository.findById(quote.getId())
				.orElseGet(QuoteEntity::new);
		mapToEntity(quote, entity);
		return mapToDomain(jpaRepository.save(entity));
	}

	@Override
	public Optional<Quote> findById(UUID id) {
		return jpaRepository.findById(id).map(this::mapToDomain);
	}

	@Override
	public Page<Quote> findAll(Pageable pageable) {
		return jpaRepository.findAll(pageable).map(this::mapToDomain);
	}

	@Override
	public List<Quote> findStaleDrafts(Instant updatedBefore) {
		return jpaRepository.findByStatusAndUpdatedAtBefore(QuoteStatus.DRAFT, updatedBefore).stream()
				.map(this::mapToDomain)
				.toList();
	}

	private void mapToEntity(Quote quote, QuoteEntity entity) {
		entity.setId(quote.getId());
		entity.setName(quote.getName());
		entity.setEmail(quote.getEmail());
		entity.setAge(quote.getAge());
		entity.setZipCode(quote.getZipCode());
		entity.setCoverageType(quote.getCoverageType());
		entity.setHasPreexistingConditions(quote.getHasPreexistingConditions());
		entity.setConditions(new HashSet<>(quote.getConditions()));
		entity.setTakesPrescriptionMedication(quote.getTakesPrescriptionMedication());
		entity.setUsesTobacco(quote.getUsesTobacco());
		entity.setNeedsSpouseCoverage(quote.getNeedsSpouseCoverage());
		entity.setEstimatedMonthlyPremium(quote.getEstimatedMonthlyPremium());
		entity.setStatus(quote.getStatus());
		entity.setCreatedAt(quote.getCreatedAt());
		entity.setUpdatedAt(quote.getUpdatedAt());
		entity.setVersion(quote.getVersion());
	}

	private Quote mapToDomain(QuoteEntity entity) {
		return Quote.reconstitute(
				entity.getId(),
				entity.getName(),
				entity.getEmail(),
				entity.getAge(),
				entity.getZipCode(),
				entity.getCoverageType(),
				entity.getHasPreexistingConditions(),
				entity.getConditions(),
				entity.getTakesPrescriptionMedication(),
				entity.getUsesTobacco(),
				entity.getNeedsSpouseCoverage(),
				entity.getEstimatedMonthlyPremium(),
				entity.getStatus(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getVersion());
	}
}
