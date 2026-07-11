package com.trustbuddy.api.quote.infrastructure.web.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.trustbuddy.api.quote.domain.exception.QuoteErrorCodes;
import com.trustbuddy.api.quote.domain.exception.QuoteValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class QuotePageablesTest {

		@Test
		void givenAllowedSort_whenRequireValid_thenReturnPageable() {
				// Given
				var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

				// When
				var actual = QuotePageables.requireValid(pageable);

				// Then
				assertThat(actual).isSameAs(pageable);
		}

		@Test
		void givenUnknownSortField_whenRequireValid_thenThrowQuoteValidationException() {
				// Given
				var pageable = PageRequest.of(0, 20, Sort.by("unknownField"));

				// When / Then
				assertThatThrownBy(() -> QuotePageables.requireValid(pageable))
								.isInstanceOf(QuoteValidationException.class)
								.hasMessage(
												"Invalid sort field 'unknownField'. Use sort=<field>,asc|desc. Allowed fields: age, createdAt, email, name, status, updatedAt")
								.extracting("errorCode")
								.isEqualTo(QuoteErrorCodes.QUOTE_INVALID_QUERY);
		}

		@Test
		void givenInvalidSortFormat_whenRequireValid_thenThrowQuoteValidationException() {
				// Given
				var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "age", "createdAt"));

				// When / Then
				assertThatThrownBy(
												() ->
																QuotePageables.requireValid(
																				pageable, java.util.List.of("age,createdAt")))
								.isInstanceOf(QuoteValidationException.class)
								.hasMessage(
												"Invalid sort format 'age,createdAt'. Use sort=<field>,asc|desc for each sort parameter. Allowed fields: age, createdAt, email, name, status, updatedAt")
								.extracting("errorCode")
								.isEqualTo(QuoteErrorCodes.QUOTE_INVALID_QUERY);
		}

		@Test
		void givenMultipleFieldsInOneSortParam_whenRequireValid_thenThrowQuoteValidationException() {
				// Given
				var pageable =
								PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "asc", "age", "createdAt"));

				// When / Then
				assertThatThrownBy(
												() ->
																QuotePageables.requireValid(
																				pageable, java.util.List.of("asc,age,createdAt")))
								.isInstanceOf(QuoteValidationException.class)
								.hasMessage(
												"Invalid sort format 'asc,age,createdAt'. Use sort=<field>,asc|desc for each sort parameter. Allowed fields: age, createdAt, email, name, status, updatedAt")
								.extracting("errorCode")
								.isEqualTo(QuoteErrorCodes.QUOTE_INVALID_QUERY);
		}

		@Test
		void givenValidSortParam_whenRequireValid_thenReturnPageable() {
				// Given
				var pageable =
								PageRequest.of(
												0, 20, Sort.by(Sort.Order.asc("status"), Sort.Order.desc("createdAt")));

				// When
				var actual =
								QuotePageables.requireValid(
												pageable, java.util.List.of("status,asc", "createdAt,desc"));

				// Then
				assertThat(actual).isSameAs(pageable);
		}

		@Test
		void givenExcessivePageSize_whenRequireValid_thenClampToMaxSize() {
				// Given
				var pageable = PageRequest.of(2, 101, Sort.by(Sort.Direction.ASC, "name"));

				// When
				Pageable actual = QuotePageables.requireValid(pageable);

				// Then
				assertThat(actual.getPageNumber()).isEqualTo(2);
				assertThat(actual.getPageSize()).isEqualTo(100);
				assertThat(actual.getSort()).isEqualTo(pageable.getSort());
		}
}
