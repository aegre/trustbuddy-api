package com.trustbuddy.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.quote")
public record QuoteProperties(int draftExpirationMinutes, long expirationJobIntervalMs, int cacheTtlMinutes) {
}
