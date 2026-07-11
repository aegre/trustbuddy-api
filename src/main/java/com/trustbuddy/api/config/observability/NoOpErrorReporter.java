package com.trustbuddy.api.config.observability;

import com.trustbuddy.api.config.port.ErrorReporterPort;

public class NoOpErrorReporter implements ErrorReporterPort {

		@Override
		public void reportUnexpected(Exception exception, String path) {
				// intentionally empty
		}

		@Override
		public void reportOperational(Exception exception, String path) {
				// intentionally empty
		}
}
