package com.trustbuddy.api.config.observability;

import com.trustbuddy.api.config.port.ErrorReporterPort;
import io.sentry.Sentry;
import io.sentry.ScopeCallback;

public class SentryErrorReporterAdapter implements ErrorReporterPort {

		@Override
		public void reportUnexpected(Exception exception, String path) {
				captureWithTags(exception, "unexpected", "500", path);
		}

		@Override
		public void reportOperational(Exception exception, String path) {
				captureWithTags(exception, "operational", "502", path);
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
