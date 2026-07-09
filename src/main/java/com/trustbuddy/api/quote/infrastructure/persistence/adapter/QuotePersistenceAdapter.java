package com.trustbuddy.api.quote.infrastructure.persistence.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.trustbuddy.api.quote.application.port.out.QuoteRepositoryPort;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.persistence.entity.QuoteEntity;
import com.trustbuddy.api.quote.infrastructure.persistence.mapper.QuotePersistenceMapper;
import com.trustbuddy.api.quote.infrastructure.persistence.repository.QuoteJpaRepository;

@Component
public class QuotePersistenceAdapter implements QuoteRepositoryPort {

	private final QuoteJpaRepository jpaRepository;
	private final QuotePersistenceMapper mapper;

	public QuotePersistenceAdapter(QuoteJpaRepository jpaRepository, QuotePersistenceMapper mapper) {
		this.jpaRepository = jpaRepository;
		this.mapper = mapper;
	}

	@Override
	public Quote save(Quote quote) {
		QuoteEntity entity = jpaRepository.findById(quote.getId())
				.orElseGet(QuoteEntity::new);
		mapper.updateEntity(quote, entity);
		return mapper.toDomain(jpaRepository.save(entity));
	}

	@Override
	public Optional<Quote> findById(UUID id) {
		return jpaRepository.findById(id).map(mapper::toDomain);
	}

	@Override
	public Page<Quote> findAll(Pageable pageable) {
		return jpaRepository.findAll(pageable).map(mapper::toDomain);
	}

	@Override
	public List<Quote> findStaleDrafts(Instant updatedBefore) {
		return jpaRepository.findByStatusAndUpdatedAtBefore(QuoteStatus.DRAFT, updatedBefore).stream()
				.map(mapper::toDomain)
				.toList();
	}
}
