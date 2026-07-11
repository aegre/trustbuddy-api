package com.trustbuddy.api.quote.infrastructure.web.support;

import com.trustbuddy.api.quote.domain.exception.QuoteErrorCodes;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.data.domain.Pageable;

public final class QuotePageables {

		public static final int MAX_SIZE = 100;

		public static final String ALLOWED_SORT_FIELDS_DOC =
						"age, createdAt, email, name, status, updatedAt";

		private static final Set<String> ALLOWED_SORT_PROPERTIES =
						Set.of("createdAt", "updatedAt", "status", "name", "email", "age");

		private QuotePageables() {}

		public static Pageable requireValid(Pageable pageable) {
				if (pageable.getPageSize() > MAX_SIZE) {
						throw new QuoteValidationException(
										QuoteErrorCodes.QUOTE_INVALID_QUERY, "size must not exceed " + MAX_SIZE);
				}
				if (pageable.getPageNumber() < 0) {
						throw new QuoteValidationException(
										QuoteErrorCodes.QUOTE_INVALID_QUERY, "page must be greater than or equal to 0");
				}
				for (var order : pageable.getSort()) {
						if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
								throw new QuoteValidationException(
												QuoteErrorCodes.QUOTE_INVALID_QUERY,
												"Invalid sort field '"
																+ order.getProperty()
																+ "'. Allowed: "
																+ allowedSortFields());
						}
				}
				return pageable;
		}

		public static String allowedSortFields() {
				return String.join(", ", new TreeSet<>(ALLOWED_SORT_PROPERTIES));
		}
}
