package com.trustbuddy.api.config.web.exception;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/test/global-exceptions")
class GlobalExceptionTestController {

		@GetMapping("/authentication")
		void authentication() {
				throw new BadCredentialsException("Invalid or missing token");
		}

		@GetMapping("/access-denied")
		void accessDenied() {
				throw new AccessDeniedException("Access is denied");
		}

		@GetMapping("/unexpected")
		void unexpected() {
				throw new IllegalStateException("Something went wrong");
		}

		@PostMapping("/validation")
		void validation(@Valid @RequestBody ValidationTestRequest request) {}
}
