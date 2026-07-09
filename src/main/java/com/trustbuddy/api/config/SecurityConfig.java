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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final JwtAuthenticationEntryPoint authenticationEntryPoint;
	private final Environment environment;

	public SecurityConfig(
			JwtAuthFilter jwtAuthFilter,
			JwtAuthenticationEntryPoint authenticationEntryPoint,
			Environment environment) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.environment = environment;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
