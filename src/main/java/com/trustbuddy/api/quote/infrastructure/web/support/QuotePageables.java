package com.trustbuddy.api.quote.infrastructure.web.support;

import com.trustbuddy.api.quote.domain.exception.QuoteErrorCodes;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class QuotePageables {

		public static final int MAX_SIZE = 100;

		public static final String ALLOWED_SORT_FIELDS_DOC =
						"age, createdAt, email, name, status, updatedAt";

		public static final String SORT_USAGE_DOC =
						"One sort per query param: sort=<field>,asc|desc. Example: sort=status,asc&sort=createdAt,desc."
										+ " Do not put multiple fields in one value (sort=age,createdAt is not two fields).";

		private static final Set<String> ALLOWED_SORT_PROPERTIES =
						Set.of("createdAt", "updatedAt", "status", "name", "email", "age");

		private QuotePageables() {}

		public static Pageable requireValid(Pageable pageable) {
				return requireValid(pageable, List.of());
		}

		public static Pageable requireValid(Pageable pageable, List<String> sortParams) {
				if (sortParams != null) {
						for (String sortParam : sortParams) {
								requireValidSortParam(sortParam);
						}
				}
				Pageable normalized = normalizePageSize(pageable);
				if (normalized.getPageNumber() < 0) {
						throw new QuoteValidationException(
										QuoteErrorCodes.QUOTE_INVALID_QUERY, "page must be greater than or equal to 0");
				}
				for (var order : normalized.getSort()) {
						if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
								throw new QuoteValidationException(
												QuoteErrorCodes.QUOTE_INVALID_QUERY,
												invalidSortFieldMessage(order.getProperty()));
						}
				}
				return normalized;
		}

		private static void requireValidSortParam(String sortParam) {
				if (sortParam == null || sortParam.isBlank()) {
						throw new QuoteValidationException(
										QuoteErrorCodes.QUOTE_INVALID_QUERY, invalidSortFormatMessage(sortParam));
				}
				String[] parts = sortParam.split(",", -1);
				if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
						throw new QuoteValidationException(
										QuoteErrorCodes.QUOTE_INVALID_QUERY, invalidSortFormatMessage(sortParam));
				}
				String field = parts[0].trim();
				String direction = parts[1].trim();
				if (!isSortDirection(direction)) {
						throw new QuoteValidationException(
										QuoteErrorCodes.QUOTE_INVALID_QUERY, invalidSortFormatMessage(sortParam));
				}
				if (!ALLOWED_SORT_PROPERTIES.contains(field)) {
						throw new QuoteValidationException(
										QuoteErrorCodes.QUOTE_INVALID_QUERY, invalidSortFieldMessage(field));
				}
		}

		private static boolean isSortDirection(String direction) {
				return "asc".equalsIgnoreCase(direction) || "desc".equalsIgnoreCase(direction);
		}

		private static Pageable normalizePageSize(Pageable pageable) {
				if (pageable.getPageSize() <= MAX_SIZE) {
						return pageable;
				}
				return PageRequest.of(pageable.getPageNumber(), MAX_SIZE, pageable.getSort());
		}

		public static String allowedSortFields() {
				return String.join(", ", new TreeSet<>(ALLOWED_SORT_PROPERTIES));
		}

		public static String invalidSortFieldMessage(String field) {
				return "Invalid sort field '"
								+ field
								+ "'. Use sort=<field>,asc|desc. Allowed fields: "
								+ allowedSortFields();
		}

		public static String invalidSortFormatMessage(String sortParam) {
				String value = sortParam == null || sortParam.isBlank() ? "(empty)" : sortParam;
				return "Invalid sort format '"
								+ value
								+ "'. Use sort=<field>,asc|desc for each sort parameter. Allowed fields: "
								+ allowedSortFields();
		}
}
