package com.trustbuddy.api.config.web.exception;

import com.trustbuddy.api.config.port.ErrorReporterPort;
import com.trustbuddy.api.config.web.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

		private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

		private final ErrorReporterPort errorReporter;

		public GlobalExceptionHandler(ErrorReporterPort errorReporter) {
				this.errorReporter = errorReporter;
		}

		@ExceptionHandler(MethodArgumentNotValidException.class)
		public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
						MethodArgumentNotValidException exception, HttpServletRequest request) {
				String message =
								exception.getBindingResult().getFieldErrors().stream()
												.map(error -> error.getField() + ": " + error.getDefaultMessage())
												.findFirst()
												.orElse("Validation failed");
				return ErrorResponseFactory.toResponseEntity(HttpStatus.BAD_REQUEST, message, request);
		}

		@ExceptionHandler(AuthenticationException.class)
		public ResponseEntity<ErrorResponse> handleAuthentication(
						AuthenticationException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
		}

		@ExceptionHandler(AccessDeniedException.class)
		public ResponseEntity<ErrorResponse> handleAccessDenied(
						AccessDeniedException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.FORBIDDEN, exception.getMessage(), request);
		}

		@ExceptionHandler(Exception.class)
		public ResponseEntity<ErrorResponse> handleUnexpected(
						Exception exception, HttpServletRequest request) {
				LOG.error(
								"Unexpected error on {} {}",
								request.getMethod(),
								request.getRequestURI(),
								exception);
				errorReporter.reportUnexpected(exception, request.getRequestURI());
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
		}
}
