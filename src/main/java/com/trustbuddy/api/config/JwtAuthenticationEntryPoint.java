package com.trustbuddy.api.config;

import com.trustbuddy.api.config.web.exception.ErrorResponseFactory;
import com.trustbuddy.api.config.web.response.ApiErrorCodes;
import com.trustbuddy.api.config.web.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.json.JsonMapper;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

		private final JsonMapper jsonMapper;

		public JwtAuthenticationEntryPoint(JsonMapper jsonMapper) {
				this.jsonMapper = jsonMapper.rebuild().build();
		}

		@Override
		public void commence(
						HttpServletRequest request,
						HttpServletResponse response,
						AuthenticationException authException)
						throws IOException {
				ErrorResponse body =
								ErrorResponseFactory.create(
												HttpStatus.UNAUTHORIZED,
												ApiErrorCodes.UNAUTHORIZED,
												resolveMessage(authException),
												request.getRequestURI());

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
