package com.trustbuddy.api.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.trustbuddy.api.domain.model.Quote;

public interface QuoteRepositoryPort {

	Quote save(Quote quote);

	Optional<Quote> findById(UUID id);

	Page<Quote> findAll(Pageable pageable);

	List<Quote> findStaleDrafts(Instant updatedBefore);
}
