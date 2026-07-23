package com.trustbuddy.api.quote.infrastructure.persistence.adapter;

import com.trustbuddy.api.quote.application.port.out.PromotionRepositoryPort;
import com.trustbuddy.api.quote.domain.model.Promotion;
import com.trustbuddy.api.quote.infrastructure.persistence.mapper.PromotionPersistenceMapper;
import com.trustbuddy.api.quote.infrastructure.persistence.repository.PromotionJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PromotionPersistenceAdapter implements PromotionRepositoryPort {

		private final PromotionJpaRepository jpaRepository;
		private final PromotionPersistenceMapper mapper;

		public PromotionPersistenceAdapter(
						PromotionJpaRepository jpaRepository, PromotionPersistenceMapper mapper) {
				this.jpaRepository = jpaRepository;
				this.mapper = mapper;
		}

		@Override
		public Optional<Promotion> findByCode(String code) {
				String normalized = Promotion.normalizeCode(code);
				return jpaRepository.findByCode(normalized).map(mapper::toDomain);
		}
}
