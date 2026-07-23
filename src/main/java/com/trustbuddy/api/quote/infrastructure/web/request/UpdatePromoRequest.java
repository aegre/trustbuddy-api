package com.trustbuddy.api.quote.infrastructure.web.request;

import static com.trustbuddy.api.quote.application.dto.UpdatePromoCommand.MAX_CODE_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Apply or replace a promotion code on a draft quote")
public class UpdatePromoRequest {

		@Schema(description = "Promotion code", example = "SAVE10", maxLength = MAX_CODE_LENGTH)
		private String code;

		public String getCode() {
				return code;
		}

		public void setCode(String code) {
				this.code = code;
		}
}
