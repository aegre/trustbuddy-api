package com.trustbuddy.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.trustbuddy.api.config.properties.QuoteProperties;

@Configuration
@EnableConfigurationProperties(QuoteProperties.class)
public class CacheConfig {

	public static final String QUOTES_CACHE = "quotes";
}
