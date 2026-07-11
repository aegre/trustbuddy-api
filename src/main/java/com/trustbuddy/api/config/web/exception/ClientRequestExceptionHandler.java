package com.trustbuddy.api.config.web.exception;

import com.trustbuddy.api.config.web.response.ApiErrorCodes;
import com.trustbuddy.api.config.web.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ClientRequestExceptionHandler {

		@ExceptionHandler(MethodArgumentTypeMismatchException.class)
		public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
						MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
				String message = typeMismatchMessage(exception);
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.BAD_REQUEST, ApiErrorCodes.INVALID_REQUEST, message, request);
		}

		@ExceptionHandler(MissingServletRequestParameterException.class)
		public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
						MissingServletRequestParameterException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.BAD_REQUEST,
								ApiErrorCodes.INVALID_REQUEST,
								"Missing required parameter '" + exception.getParameterName() + "'",
								request);
		}

		@ExceptionHandler(HttpMessageNotReadableException.class)
		public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
						HttpMessageNotReadableException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.BAD_REQUEST,
								ApiErrorCodes.INVALID_REQUEST,
								"Malformed request body",
								request);
		}

		@ExceptionHandler(PropertyReferenceException.class)
		public ResponseEntity<ErrorResponse> handlePropertyReference(
						PropertyReferenceException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.BAD_REQUEST,
								ApiErrorCodes.INVALID_REQUEST,
								"Invalid query parameter: " + exception.getMessage(),
								request);
		}

		@ExceptionHandler(InvalidDataAccessApiUsageException.class)
		public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsage(
						InvalidDataAccessApiUsageException exception, HttpServletRequest request) {
				return ErrorResponseFactory.toResponseEntity(
								HttpStatus.BAD_REQUEST,
								ApiErrorCodes.INVALID_REQUEST,
								"Invalid query parameter: " + exception.getMessage(),
								request);
		}

		private static String typeMismatchMessage(MethodArgumentTypeMismatchException exception) {
				if (exception.getRequiredType() == UUID.class) {
						return exception.getName() + " must be a valid UUID";
				}
				return "Invalid value for parameter '" + exception.getName() + "'";
		}
}
