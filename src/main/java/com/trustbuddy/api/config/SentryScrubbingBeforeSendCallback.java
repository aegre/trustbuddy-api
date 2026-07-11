package com.trustbuddy.api.config;

import io.sentry.Hint;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SentryScrubbingBeforeSendCallback implements SentryOptions.BeforeSendCallback {

		private static final String REDACTED = "[REDACTED]";

		private static final Set<String> SENSITIVE_HEADERS =
						Set.of("authorization", "cookie", "set-cookie");

		private static final Pattern SENSITIVE_JSON_FIELD =
						Pattern.compile(
										"(\"(?:password|secret|token|accessToken)\"\\s*:\\s*)\"[^\"]*\"",
										Pattern.CASE_INSENSITIVE);

		@Override
		public SentryEvent execute(SentryEvent event, Hint hint) {
				Request request = event.getRequest();
				if (request == null) {
						return event;
				}

				scrubHeaders(request);
				scrubCookies(request);
				scrubRequestData(request);
				return event;
		}

		private void scrubHeaders(Request request) {
				Map<String, String> headers = request.getHeaders();
				if (headers == null || headers.isEmpty()) {
						return;
				}

				Map<String, String> scrubbed = new HashMap<>();
				for (Map.Entry<String, String> entry : headers.entrySet()) {
						if (isSensitiveHeader(entry.getKey())) {
								scrubbed.put(entry.getKey(), REDACTED);
						} else {
								scrubbed.put(entry.getKey(), entry.getValue());
						}
				}
				request.setHeaders(scrubbed);
		}

		private void scrubCookies(Request request) {
				if (request.getCookies() != null) {
						request.setCookies(REDACTED);
				}
		}

		private void scrubRequestData(Request request) {
				Object data = request.getData();
				if (data instanceof String body && !body.isBlank()) {
						request.setData(SENSITIVE_JSON_FIELD.matcher(body).replaceAll("$1\"" + REDACTED + "\""));
				}
		}

		private boolean isSensitiveHeader(String headerName) {
				return SENSITIVE_HEADERS.contains(headerName.toLowerCase(Locale.ROOT));
		}
}
