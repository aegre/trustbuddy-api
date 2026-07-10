package com.trustbuddy.api.config;

import com.trustbuddy.api.config.properties.CorsProperties;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

		@Bean
		CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
				CorsConfiguration configuration = new CorsConfiguration();
				configuration.setAllowedOrigins(corsProperties.allowedOrigins());
				configuration.setAllowedMethods(
								List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
				configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
				configuration.setAllowCredentials(true);
				configuration.setMaxAge(3600L);

				UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
				source.registerCorsConfiguration("/**", configuration);
				return source;
		}
}
