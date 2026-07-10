package com.trustbuddy.api.quote.infrastructure.web.exception;

import com.trustbuddy.api.quote.domain.exception.ConditionalFieldRejectedException;
import com.trustbuddy.api.quote.domain.exception.DomainException;
import com.trustbuddy.api.quote.domain.exception.ExternalSubmissionException;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import com.trustbuddy.api.quote.infrastructure.web.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

		@ExceptionHandler(QuoteNotFoundException.class)
		public ResponseEntity<ErrorResponse> handleQuoteNotFound(
						QuoteNotFoundException exception, HttpServletRequest request) {
				return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request);
		}

		@ExceptionHandler(InvalidQuoteStateException.class)
		public ResponseEntity<ErrorResponse> handleInvalidQuoteState(
						InvalidQuoteStateException exception, HttpServletRequest request) {
				return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request);
		}

		@ExceptionHandler({QuoteValidationException.class, ConditionalFieldRejectedException.class})
		public ResponseEntity<ErrorResponse> handleQuoteValidation(
						DomainException exception, HttpServletRequest request) {
				return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
		}

		@ExceptionHandler(ExternalSubmissionException.class)
		public ResponseEntity<ErrorResponse> handleExternalSubmission(
						ExternalSubmissionException exception, HttpServletRequest request) {
				return buildErrorResponse(HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
		}

		@ExceptionHandler(MethodArgumentNotValidException.class)
		public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
						MethodArgumentNotValidException exception, HttpServletRequest request) {
				String message =
								exception.getBindingResult().getFieldErrors().stream()
												.map(error -> error.getField() + ": " + error.getDefaultMessage())
												.findFirst()
												.orElse("Validation failed");
				return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
		}

		@ExceptionHandler(AuthenticationException.class)
		public ResponseEntity<ErrorResponse> handleAuthentication(
						AuthenticationException exception, HttpServletRequest request) {
				return buildErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
		}

		@ExceptionHandler(AccessDeniedException.class)
		public ResponseEntity<ErrorResponse> handleAccessDenied(
						AccessDeniedException exception, HttpServletRequest request) {
				return buildErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request);
		}

		private ResponseEntity<ErrorResponse> buildErrorResponse(
						HttpStatus status, String message, HttpServletRequest request) {
				ErrorResponse body = new ErrorResponse();
				body.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
				body.setStatus(status.value());
				body.setError(status.getReasonPhrase());
				body.setMessage(message);
				body.setPath(request.getRequestURI());
				return ResponseEntity.status(status).body(body);
		}
}
