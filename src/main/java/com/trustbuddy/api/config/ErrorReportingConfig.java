package com.trustbuddy.api.config;

import com.trustbuddy.api.config.observability.NoOpErrorReporter;
import com.trustbuddy.api.config.observability.SentryErrorReporterAdapter;
import com.trustbuddy.api.config.port.ErrorReporterPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErrorReportingConfig {

		@Bean
		@ConditionalOnProperty(name = "sentry.enabled", havingValue = "true")
		ErrorReporterPort sentryErrorReporterPort() {
				return new SentryErrorReporterAdapter();
		}

		@Bean
		@ConditionalOnProperty(name = "sentry.enabled", havingValue = "false", matchIfMissing = true)
		ErrorReporterPort noOpErrorReporterPort() {
				return new NoOpErrorReporter();
		}
}
