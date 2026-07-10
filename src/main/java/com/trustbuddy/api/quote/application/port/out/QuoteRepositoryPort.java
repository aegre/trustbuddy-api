package com.trustbuddy.api.quote.application.port.out;

import com.trustbuddy.api.quote.domain.model.Quote;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuoteRepositoryPort {

		Quote save(Quote quote);

		Optional<Quote> findById(UUID id);

		Page<Quote> findAll(Pageable pageable);

		List<Quote> findStaleDrafts(Instant updatedBefore);
}
