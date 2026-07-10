package com.trustbuddy.api.config;

import com.trustbuddy.api.config.properties.QuoteProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(QuoteProperties.class)
public class CacheConfig {

		public static final String QUOTES_CACHE = "quotes";
}
