package com.trustbuddy.api.quote.infrastructure.web.mapper;

import com.trustbuddy.api.quote.application.dto.CreateQuoteCommand;
import com.trustbuddy.api.quote.application.dto.UpdateCoverageCommand;
import com.trustbuddy.api.quote.application.dto.UpdatePersonalInfoCommand;
import com.trustbuddy.api.quote.application.dto.UpdatePromoCommand;
import com.trustbuddy.api.quote.domain.model.AppliedPromotion;
import com.trustbuddy.api.quote.domain.model.Quote;
import com.trustbuddy.api.quote.infrastructure.web.request.CreateQuoteRequest;
import com.trustbuddy.api.quote.infrastructure.web.request.UpdateCoverageRequest;
import com.trustbuddy.api.quote.infrastructure.web.request.UpdatePersonalInfoRequest;
import com.trustbuddy.api.quote.infrastructure.web.request.UpdatePromoRequest;
import com.trustbuddy.api.quote.infrastructure.web.response.QuoteResponse;
import java.util.LinkedHashSet;

public final class QuoteWebMapper {

		private QuoteWebMapper() {}

		public static CreateQuoteCommand toCommand(CreateQuoteRequest request) {
				CreateQuoteCommand command = new CreateQuoteCommand();
				command.setName(request.getName());
				command.setEmail(request.getEmail());
				command.setAge(request.getAge());
				command.setZipCode(request.getZipCode());
				return command;
		}

		public static UpdateCoverageCommand toCommand(UpdateCoverageRequest request) {
				UpdateCoverageCommand command = new UpdateCoverageCommand();
				command.setCoverageType(request.getCoverageType());
				command.setHasPreexistingConditions(request.getHasPreexistingConditions());
				command.setConditions(request.getConditions());
				command.setTakesPrescriptionMedication(request.getTakesPrescriptionMedication());
				command.setUsesTobacco(request.getUsesTobacco());
				command.setNeedsSpouseCoverage(request.getNeedsSpouseCoverage());
				return command;
		}

		public static UpdatePersonalInfoCommand toCommand(UpdatePersonalInfoRequest request) {
				UpdatePersonalInfoCommand command = new UpdatePersonalInfoCommand();
				command.setName(request.getName());
				command.setEmail(request.getEmail());
				command.setAge(request.getAge());
				command.setZipCode(request.getZipCode());
				return command;
		}

		public static UpdatePromoCommand toCommand(UpdatePromoRequest request) {
				UpdatePromoCommand command = new UpdatePromoCommand();
				command.setCode(request.getCode());
				return command;
		}

		public static QuoteResponse toResponse(Quote quote) {
				QuoteResponse response = new QuoteResponse();
				response.setId(quote.getId());
				response.setName(quote.getName());
				response.setEmail(quote.getEmail());
				response.setAge(quote.getAge());
				response.setZipCode(quote.getZipCode());
				response.setCoverageType(quote.getCoverageType());
				response.setHasPreexistingConditions(quote.getHasPreexistingConditions());
				response.setConditions(new LinkedHashSet<>(quote.getConditions()));
				response.setTakesPrescriptionMedication(quote.getTakesPrescriptionMedication());
				response.setUsesTobacco(quote.getUsesTobacco());
				response.setNeedsSpouseCoverage(quote.getNeedsSpouseCoverage());
				response.setEstimatedMonthlyPremium(quote.getEstimatedMonthlyPremium());
				AppliedPromotion appliedPromotion = quote.getAppliedPromotion();
				if (appliedPromotion != null) {
						response.setPromoCode(appliedPromotion.code());
						response.setPromotionPercentage(appliedPromotion.percentage());
						response.setDiscountAmount(appliedPromotion.discountAmount());
				}
				response.setStatus(quote.getStatus());
				response.setCreatedAt(quote.getCreatedAt());
				response.setUpdatedAt(quote.getUpdatedAt());
				response.setVersion(quote.getVersion());
				return response;
		}
}
