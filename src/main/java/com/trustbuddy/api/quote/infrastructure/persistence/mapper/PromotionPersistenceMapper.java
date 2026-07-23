package com.trustbuddy.api.quote.infrastructure.persistence.mapper;

import com.trustbuddy.api.quote.domain.model.Promotion;
import com.trustbuddy.api.quote.infrastructure.persistence.entity.PromotionEntity;
import org.springframework.stereotype.Component;

@Component
public class PromotionPersistenceMapper {

		public Promotion toDomain(PromotionEntity entity) {
				return new Promotion(
								entity.getId(),
								entity.getCode(),
								entity.getPercentage(),
								entity.isActive(),
								entity.getStartsAt(),
								entity.getEndsAt());
		}

		public void updateEntity(Promotion promotion, PromotionEntity entity) {
				entity.setId(promotion.id());
				entity.setCode(promotion.code());
				entity.setPercentage(promotion.percentage());
				entity.setActive(promotion.active());
				entity.setStartsAt(promotion.startsAt());
				entity.setEndsAt(promotion.endsAt());
		}
}
