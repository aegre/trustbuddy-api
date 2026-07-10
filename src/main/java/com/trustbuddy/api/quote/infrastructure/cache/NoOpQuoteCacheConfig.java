package com.trustbuddy.api.quote.infrastructure.cache;

import com.trustbuddy.api.quote.application.port.out.QuoteCachePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NoOpQuoteCacheConfig {

		@Bean
		@ConditionalOnMissingBean(QuoteCachePort.class)
		QuoteCachePort noOpQuoteCachePort() {
				return new NoOpQuoteCacheAdapter();
		}
}
