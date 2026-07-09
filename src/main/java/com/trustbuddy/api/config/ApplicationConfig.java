package com.trustbuddy.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.trustbuddy.api.config.properties.AuthProperties;
import com.trustbuddy.api.config.properties.JwtProperties;
import com.trustbuddy.api.config.properties.KafkaProperties;
import com.trustbuddy.api.config.properties.QuoteProperties;

/**
 * Central place for Spring bean wiring (ports to adapters) in later phases.
 */
@Configuration
@EnableConfigurationProperties({
		JwtProperties.class,
		AuthProperties.class,
		QuoteProperties.class,
		KafkaProperties.class
})
public class ApplicationConfig {
}
