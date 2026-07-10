package com.trustbuddy.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustbuddy.api.testsupport.PostgresRedisTestcontainers;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		properties = {
			"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
			"springdoc.packages-to-scan=com.trustbuddy.api.config.web,com.trustbuddy.api.quote.infrastructure.web.controller"
		})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiSpecDriftTest extends PostgresRedisTestcontainers {

	private static final Path OPENAPI_SPEC_PATH = Path.of("openapi", "openapi.json");

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Test
	void givenCommittedOpenApiSpec_whenCompareToApiDocs_thenStayInSync() throws Exception {
		// Given
		assertThat(OPENAPI_SPEC_PATH)
				.as("Run make openapi-export to create %s", OPENAPI_SPEC_PATH)
				.isRegularFile();
		JsonNode committed =
				OBJECT_MAPPER.readTree(Files.readString(OPENAPI_SPEC_PATH));

		// When
		MvcResult result = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
		JsonNode live = OBJECT_MAPPER.readTree(result.getResponse().getContentAsString());

		// Then
		assertThat(live)
				.as("openapi/openapi.json is out of date — run make openapi-export and commit the file")
				.isEqualTo(committed);
	}
}
