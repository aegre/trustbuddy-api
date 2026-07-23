package com.trustbuddy.api.quote.application.port.out;

import com.trustbuddy.api.quote.domain.model.Promotion;
import java.util.Optional;

public interface PromotionRepositoryPort {

		Optional<Promotion> findByCode(String code);
}
