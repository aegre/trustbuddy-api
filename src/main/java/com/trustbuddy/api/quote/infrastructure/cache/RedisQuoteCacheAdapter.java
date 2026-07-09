package com.trustbuddy.api.quote.infrastructure.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;

import com.trustbuddy.api.config.CacheConfig;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import com.trustbuddy.api.quote.domain.model.Quote;

class RedisQuoteCacheAdapter implements QuoteCachePort {

	private final RedisTemplate<String, String> redisTemplate;
	private final QuoteCacheMapper mapper;
	private final Duration cacheTtl;

	RedisQuoteCacheAdapter(
			RedisTemplate<String, String> quoteRedisTemplate,
			QuoteCacheMapper mapper,
			Duration quoteCacheTtl) {
		this.redisTemplate = quoteRedisTemplate;
		this.mapper = mapper;
		this.cacheTtl = quoteCacheTtl;
	}

	@Override
	public Optional<Quote> get(UUID id) {
		String json = redisTemplate.opsForValue().get(cacheKey(id));
		if (json == null) {
			return Optional.empty();
		}
		return Optional.of(mapper.fromJson(json));
	}

	@Override
	public void put(Quote quote) {
		String key = cacheKey(quote.getId());
		String json = mapper.toJson(quote);
		if (cacheTtl.isZero()) {
			redisTemplate.opsForValue().set(key, json);
			return;
		}
		redisTemplate.opsForValue().set(key, json, cacheTtl);
	}

	@Override
	public void evict(UUID id) {
		redisTemplate.delete(cacheKey(id));
	}

	private static String cacheKey(UUID id) {
		return CacheConfig.QUOTES_CACHE + ":" + id;
	}
}
