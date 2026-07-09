package com.trustbuddy.api.quote.infrastructure.web.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.trustbuddy.api.quote.application.service.QuoteService;
import com.trustbuddy.api.quote.application.service.QuoteSubmissionService;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.infrastructure.web.mapper.QuoteWebMapper;
import com.trustbuddy.api.quote.infrastructure.web.request.CreateQuoteRequest;
import com.trustbuddy.api.quote.infrastructure.web.request.UpdateCoverageRequest;
import com.trustbuddy.api.quote.infrastructure.web.response.QuoteResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/quotes")
@Tag(name = "Quotes")
public class QuoteController {

	private final QuoteService quoteService;
	private final QuoteSubmissionService quoteSubmissionService;

	public QuoteController(QuoteService quoteService, QuoteSubmissionService quoteSubmissionService) {
		this.quoteService = quoteService;
		this.quoteSubmissionService = quoteSubmissionService;
	}

	@PostMapping
	@Operation(summary = "Create a draft quote from personal information")
	public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody CreateQuoteRequest request) {
		Quote created = quoteService.createQuote(
				request.getName(),
				request.getEmail(),
				request.getAge(),
				request.getZipCode());
		QuoteResponse response = QuoteWebMapper.toResponse(created);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.getId())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@PatchMapping("/{id}/coverage")
	@Operation(summary = "Set coverage type and supplemental health answers")
	public QuoteResponse updateCoverage(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateCoverageRequest request) {
		Quote updated = quoteService.updateCoverage(
				id,
				request.getCoverageType(),
				request.getHasPreexistingConditions(),
				request.getConditions(),
				request.getTakesPrescriptionMedication(),
				request.getUsesTobacco(),
				request.getNeedsSpouseCoverage());
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
	@Operation(summary = "List quotes with pagination")
	public Page<QuoteResponse> listQuotes(@PageableDefault(size = 20) Pageable pageable) {
		return quoteService.listQuotes(pageable).map(QuoteWebMapper::toResponse);
	}
}
