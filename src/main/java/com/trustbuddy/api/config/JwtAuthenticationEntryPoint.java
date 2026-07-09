package com.trustbuddy.api.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustbuddy.api.quote.infrastructure.web.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		ErrorResponse body = new ErrorResponse();
		body.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
		body.setStatus(HttpStatus.UNAUTHORIZED.value());
		body.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
		body.setMessage(resolveMessage(authException));
		body.setPath(request.getRequestURI());

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), body);
	}

	private String resolveMessage(AuthenticationException authException) {
		if (authException.getMessage() != null && !authException.getMessage().isBlank()) {
			return authException.getMessage();
		}
		return "Authentication required";
	}
}
