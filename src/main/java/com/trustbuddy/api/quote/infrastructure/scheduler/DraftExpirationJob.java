package com.trustbuddy.api.quote.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.trustbuddy.api.quote.application.service.DraftExpirationService;

@Component
public class DraftExpirationJob {

	private final DraftExpirationService draftExpirationService;

	public DraftExpirationJob(DraftExpirationService draftExpirationService) {
		this.draftExpirationService = draftExpirationService;
	}

	@Scheduled(fixedDelayString = "${app.quote.expiration-job-interval-ms}")
	public void expireStaleDrafts() {
		draftExpirationService.expireStaleDrafts();
	}
}
