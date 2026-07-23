package com.trustbuddy.api.quote.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePromoCommand {

		public static final int MAX_CODE_LENGTH = 64;

		@NotBlank
		@Size(max = MAX_CODE_LENGTH)
		private String code;

		public String getCode() {
				return code;
		}

		public void setCode(String code) {
				this.code = code;
		}
}
