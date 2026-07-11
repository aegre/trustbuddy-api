package com.trustbuddy.api.config.port;

public interface ErrorReporterPort {

		void reportUnexpected(Exception exception, String path);

		void reportOperational(Exception exception, String path);
}
