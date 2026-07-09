package com.trustbuddy.api.quote.infrastructure.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.trustbuddy.api.quote.application.port.out.InsurerGatewayPort;
import com.trustbuddy.api.quote.application.port.out.InsurerSubmissionResult;
import com.trustbuddy.api.quote.domain.model.Quote;

@Component
public class InsurerGatewayHttpAdapter implements InsurerGatewayPort {

	private final RestClient restClient;
	private final String gatewayUrl;

	@Autowired
	public InsurerGatewayHttpAdapter(
			@Value("${app.insurer.gateway.url}") String gatewayUrl,
			@Value("${app.insurer.gateway.timeout-ms}") int timeoutMs) {
		this.gatewayUrl = gatewayUrl;
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
		requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));
		this.restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.build();
	}

	InsurerGatewayHttpAdapter(RestClient restClient, String gatewayUrl) {
		this.restClient = restClient;
		this.gatewayUrl = gatewayUrl;
	}

	@Override
	public InsurerSubmissionResult submit(Quote quote) {
		try {
			return restClient.get()
					.uri(gatewayUrl)
					.exchange((request, response) -> {
						int status = response.getStatusCode().value();
						boolean success = response.getStatusCode().is2xxSuccessful();
						String message = success
								? "Insurer accepted quote " + quote.getId()
								: "Insurer gateway returned HTTP " + status;
						return new InsurerSubmissionResult(success, status, message);
					});
		} catch (RestClientException exception) {
			return new InsurerSubmissionResult(false, 0, exception.getMessage());
		}
	}
}
