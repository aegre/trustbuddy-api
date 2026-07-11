package com.trustbuddy.api.quote.infrastructure.web.controller;

import com.trustbuddy.api.config.web.ApiPaths;
import com.trustbuddy.api.quote.application.service.QuoteService;
import com.trustbuddy.api.quote.application.service.QuoteSubmissionService;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.infrastructure.web.mapper.QuoteWebMapper;
import com.trustbuddy.api.quote.infrastructure.web.request.CreateQuoteRequest;
import com.trustbuddy.api.quote.infrastructure.web.request.UpdateCoverageRequest;
import com.trustbuddy.api.quote.infrastructure.web.request.UpdatePersonalInfoRequest;
import com.trustbuddy.api.quote.infrastructure.web.response.QuoteResponse;
import com.trustbuddy.api.quote.infrastructure.web.support.QuotePageables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(ApiPaths.QUOTES)
@Tag(name = "Quotes")
public class QuoteController {

		private final QuoteService quoteService;
		private final QuoteSubmissionService quoteSubmissionService;

		public QuoteController(
						QuoteService quoteService, QuoteSubmissionService quoteSubmissionService) {
				this.quoteService = quoteService;
				this.quoteSubmissionService = quoteSubmissionService;
		}

		@PostMapping
		@Operation(summary = "Create a draft quote from personal information")
		public ResponseEntity<QuoteResponse> createQuote(@RequestBody CreateQuoteRequest request) {
				Quote created = quoteService.createQuote(QuoteWebMapper.toCommand(request));
				QuoteResponse response = QuoteWebMapper.toResponse(created);
				URI location =
								ServletUriComponentsBuilder.fromCurrentRequest()
												.path("/{id}")
												.buildAndExpand(created.getId())
												.toUri();
				return ResponseEntity.created(location).body(response);
		}

		@PatchMapping("/{id}")
		@Operation(summary = "Update personal information on a draft quote")
		public QuoteResponse updatePersonalInfo(
						@PathVariable UUID id, @RequestBody UpdatePersonalInfoRequest request) {
				Quote updated = quoteService.updatePersonalInfo(id, QuoteWebMapper.toCommand(request));
				return QuoteWebMapper.toResponse(updated);
		}

		@PatchMapping("/{id}/coverage")
		@Operation(summary = "Partially update coverage type and supplemental health answers")
		public QuoteResponse updateCoverage(
						@PathVariable UUID id, @RequestBody UpdateCoverageRequest request) {
				Quote updated = quoteService.updateCoverage(id, QuoteWebMapper.toCommand(request));
				return QuoteWebMapper.toResponse(updated);
		}

		@PostMapping("/{id}/submit")
		@Operation(summary = "Submit a completed quote to the external insurer gateway")
		public QuoteResponse submitQuote(@PathVariable UUID id) {
				Quote submitted = quoteSubmissionService.submitQuote(id);
				return QuoteWebMapper.toResponse(submitted);
		}

		@GetMapping("/{id}")
		@Operation(summary = "Get a quote by id")
		public QuoteResponse getQuote(@PathVariable UUID id) {
				return QuoteWebMapper.toResponse(quoteService.getQuote(id));
		}

		@GetMapping
		@Operation(
						summary = "List quotes with pagination",
						description =
										"Query params: page (0-based), size (capped at "
														+ QuotePageables.MAX_SIZE
														+ "), sort (<field>,asc|desc — repeat sort for multiple fields). "
														+ "Allowed fields: "
														+ QuotePageables.ALLOWED_SORT_FIELDS_DOC
														+ ". "
														+ QuotePageables.SORT_USAGE_DOC
														+ " Default: createdAt,desc.")
		public Page<QuoteResponse> listQuotes(
						HttpServletRequest request,
						@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
										Pageable pageable) {
				List<String> sortParams =
								request.getParameterValues("sort") == null
												? List.of()
												: Arrays.asList(request.getParameterValues("sort"));
				return quoteService
								.listQuotes(QuotePageables.requireValid(pageable, sortParams))
								.map(QuoteWebMapper::toResponse);
		}
}
