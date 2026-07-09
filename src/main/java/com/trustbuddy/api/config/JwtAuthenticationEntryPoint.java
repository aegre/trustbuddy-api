package com.trustbuddy.api.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.trustbuddy.api.quote.infrastructure.web.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final JsonMapper jsonMapper;

	public JwtAuthenticationEntryPoint(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
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
		jsonMapper.writeValue(response.getOutputStream(), body);
	}

	private String resolveMessage(AuthenticationException authException) {
		if (authException.getMessage() != null && !authException.getMessage().isBlank()) {
			return authException.getMessage();
		}
		return "Authentication required";
	}
}
