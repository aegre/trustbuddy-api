package com.trustbuddy.api.quote.infrastructure.persistence.repository;

import com.trustbuddy.api.quote.infrastructure.persistence.entity.PromotionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionJpaRepository extends JpaRepository<PromotionEntity, UUID> {

		Optional<PromotionEntity> findByCode(String code);
}
