package com.trustbuddy.api.quote.infrastructure.web.exception;

import com.trustbuddy.api.config.SentryErrorReporter;
import com.trustbuddy.api.config.web.exception.ErrorResponseFactory;
import com.trustbuddy.api.config.web.response.ErrorResponse;
import com.trustbuddy.api.quote.domain.exception.ConditionalFieldRejectedException;
import com.trustbuddy.api.quote.domain.exception.DomainException;
import com.trustbuddy.api.quote.domain.exception.ExternalSubmissionException;
import com.trustbuddy.api.quote.domain.exception.InvalidQuoteStateException;
import com.trustbuddy.api.quote.domain.exception.QuoteNotFoundException;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.trustbuddy.api.quote.infrastructure.web")
public class QuoteExceptionHandler {

		private final SentryErrorReporter sentryErrorReporter;

		public QuoteExceptionHandler(SentryErrorReporter sentryErrorReporter) {
				this.sentryErrorReporter = sentryErrorReporter;
		}

		@ExceptionHandler(QuoteNotFoundException.class)
		public ResponseEntity<ErrorResponse> handleQuoteNotFound(
						QuoteNotFoundException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.NOT_FOUND, exception.getMessage(), request);
		}

		@ExceptionHandler(InvalidQuoteStateException.class)
		public ResponseEntity<ErrorResponse> handleInvalidQuoteState(
						InvalidQuoteStateException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.CONFLICT, exception.getMessage(), request);
		}

		@ExceptionHandler({QuoteValidationException.class, ConditionalFieldRejectedException.class})
		public ResponseEntity<ErrorResponse> handleQuoteValidation(
						DomainException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.BAD_REQUEST, exception.getMessage(), request);
		}

		@ExceptionHandler(ExternalSubmissionException.class)
		public ResponseEntity<ErrorResponse> handleExternalSubmission(
						ExternalSubmissionException exception, HttpServletRequest request) {
				sentryErrorReporter.reportOperational(exception, request);
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.BAD_GATEWAY, exception.getMessage(), request);
		}
}
