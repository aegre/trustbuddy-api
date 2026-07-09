package com.trustbuddy.api.quote.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.persistence.entity.QuoteEntity;

public interface QuoteJpaRepository extends JpaRepository<QuoteEntity, UUID> {

	List<QuoteEntity> findByStatusAndUpdatedAtBefore(QuoteStatus status, Instant updatedBefore);
}
