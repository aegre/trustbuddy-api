package com.trustbuddy.api.adapter.out.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trustbuddy.api.adapter.out.persistence.entity.QuoteEntity;
import com.trustbuddy.api.domain.model.QuoteStatus;

public interface QuoteJpaRepository extends JpaRepository<QuoteEntity, UUID> {

	List<QuoteEntity> findByStatusAndUpdatedAtBefore(QuoteStatus status, Instant updatedBefore);
}
