package com.trustbuddy.api.config.web.exception;

import com.trustbuddy.api.config.web.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public final class ErrorResponseFactory {

		private ErrorResponseFactory() {}

		public static ErrorResponse create(HttpStatus status, String code, String message, String path) {
				ErrorResponse body = new ErrorResponse();
				body.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
				body.setStatus(status.value());
				body.setError(status.getReasonPhrase());
				body.setCode(code);
				body.setMessage(message);
				body.setPath(path);
				return body;
		}

		public static ResponseEntity<ErrorResponse> toResponseEntity(
						HttpStatus status, String code, String message, HttpServletRequest request) {
				return ResponseEntity.status(status)
								.body(create(status, code, message, request.getRequestURI()));
		}
}
