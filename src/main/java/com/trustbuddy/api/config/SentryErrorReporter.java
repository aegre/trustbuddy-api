package com.trustbuddy.api.config;

import com.trustbuddy.api.quote.domain.exception.ExternalSubmissionException;
import io.sentry.Sentry;
import io.sentry.ScopeCallback;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class SentryErrorReporter {

		public void reportUnexpected(Exception exception, HttpServletRequest request) {
				captureWithTags(exception, "unexpected", "500", request.getRequestURI());
		}

		public void reportOperational(
						ExternalSubmissionException exception, HttpServletRequest request) {
				captureWithTags(exception, "operational", "502", request.getRequestURI());
		}

		private void captureWithTags(
						Throwable exception, String errorType, String httpStatus, String path) {
				ScopeCallback scopeCallback =
								scope -> {
										scope.setTag("error.type", errorType);
										scope.setTag("http.status", httpStatus);
										scope.setTag("path", path);
								};
				Sentry.captureException(exception, scopeCallback);
		}
}
