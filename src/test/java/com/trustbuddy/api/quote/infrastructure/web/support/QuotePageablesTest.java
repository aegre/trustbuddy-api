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
								.hasMessageContaining("Invalid sort field 'unknownField'")
								.extracting("errorCode")
								.isEqualTo(QuoteErrorCodes.QUOTE_INVALID_QUERY);
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
