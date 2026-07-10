package com.trustbuddy.api.quote.infrastructure.persistence.repository;

import com.trustbuddy.api.quote.domain.model.QuoteStatus;
import com.trustbuddy.api.quote.infrastructure.persistence.entity.QuoteEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteJpaRepository extends JpaRepository<QuoteEntity, UUID> {

		List<QuoteEntity> findByStatusAndUpdatedAtBefore(QuoteStatus status, Instant updatedBefore);
}
