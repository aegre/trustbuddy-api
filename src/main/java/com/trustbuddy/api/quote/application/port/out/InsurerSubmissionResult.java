package com.trustbuddy.api.quote.application.port.out;

public record InsurerSubmissionResult(boolean success, int httpStatus, String message) {}
