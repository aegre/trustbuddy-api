package com.trustbuddy.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt.cookie")
public record JwtCookieProperties(String name, boolean secure, String sameSite) {}
