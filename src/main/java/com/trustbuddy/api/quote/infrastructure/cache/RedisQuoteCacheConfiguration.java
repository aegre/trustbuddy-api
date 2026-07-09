package com.trustbuddy.api.quote.infrastructure.cache;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.trustbuddy.api.config.properties.QuoteProperties;
import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;

@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
class RedisQuoteCacheConfiguration {

	@Bean
	RedisTemplate<String, String> quoteRedisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	Duration quoteCacheTtl(QuoteProperties quoteProperties) {
		int ttlMinutes = quoteProperties.cacheTtlMinutes();
		if (ttlMinutes <= 0) {
			return Duration.ZERO;
		}
		return Duration.ofMinutes(ttlMinutes);
	}

	@Bean
	@ConditionalOnMissingBean(QuoteCachePort.class)
	QuoteCachePort redisQuoteCacheAdapter(
			RedisTemplate<String, String> quoteRedisTemplate,
			QuoteCacheMapper mapper,
			Duration quoteCacheTtl) {
		return new RedisQuoteCacheAdapter(quoteRedisTemplate, mapper, quoteCacheTtl);
	}
}
