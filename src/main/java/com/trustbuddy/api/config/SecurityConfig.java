package com.trustbuddy.api.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final Environment environment;

	public SecurityConfig(Environment environment) {
		this.environment = environment;
	}

	@Bean
	JwtAuthFilter jwtAuthFilter(JwtService jwtService) {
		return new JwtAuthFilter(jwtService);
	}

	@Bean
	JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(JsonMapper jsonMapper) {
		return new JwtAuthenticationEntryPoint(jsonMapper);
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthFilter jwtAuthFilter,
			JwtAuthenticationEntryPoint authenticationEntryPoint) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
				.authorizeHttpRequests(auth -> {
					auth.requestMatchers("/auth/token").permitAll();
					auth.requestMatchers("/actuator/health", "/actuator/info").permitAll();
					if (isSwaggerEnabled()) {
						auth.requestMatchers(
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**")
								.permitAll();
					}
					auth.requestMatchers("/quotes/**").authenticated();
					auth.anyRequest().denyAll();
				})
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private boolean isSwaggerEnabled() {
		return Arrays.stream(environment.getActiveProfiles()).noneMatch("prod"::equals);
	}
}
