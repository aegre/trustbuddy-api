package com.trustbuddy.api.config.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated principal resolved from Bearer JWT or access-token cookie")
public record AuthMeResponse(@Schema(example = "dev-user") String username) {}
