package com.trustbuddy.api.config.web.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

class ValidationTestRequest {

		@NotBlank private String name;

		@NotBlank @Email private String email;

		public String getName() {
				return name;
		}

		public void setName(String name) {
				this.name = name;
		}

		public String getEmail() {
				return email;
		}

		public void setEmail(String email) {
				this.email = email;
		}
}
