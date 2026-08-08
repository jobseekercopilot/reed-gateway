package com.jobseekercopilot.reedgateway.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jobseekercopilot.reedgateway.config.ReedApiProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;

@ExtendWith(OutputCaptureExtension.class)
class ReedApiClientTest {

    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicReference<URI> requestedUri = new AtomicReference<>();
    private final AtomicReference<String> authorization =
            new AtomicReference<>();
    private HttpServer server;
    private ReedApiProperties properties;

    @BeforeEach
    void startProviderStub() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::respond);
        server.start();

        properties = new ReedApiProperties();
        properties.setBaseUrl(
                "http://127.0.0.1:" + server.getAddress().getPort());
        properties.setKey("synthetic-key");
        properties.setEnabled(true);
        properties.setSearchPath("/search");
    }

    @AfterEach
    void stopProviderStub() {
        server.stop(0);
    }

    @Test
    void sendsCredentialAsBasicAuthWithoutPuttingItInTheUri() {
        var response = search(new ReedApiClient(properties));

        String expectedAuthorization = "Basic " + Base64.getEncoder()
                .encodeToString(
                        "synthetic-key:".getBytes(StandardCharsets.UTF_8));
        assertThat(response.getResults()).isEmpty();
        assertThat(requestedUri.get().getPath()).isEqualTo("/search");
        assertThat(requestedUri.get().getRawQuery())
                .contains("keywords=Platform%20Engineer")
                .contains("locationName=London")
                .contains("permanent=true")
                .contains("resultsToSkip=0")
                .doesNotContain("employmentType")
                .doesNotContain("currency")
                .doesNotContain("page=")
                .doesNotContain("synthetic-key");
        assertThat(authorization).hasValue(expectedAuthorization);
    }

    @Test
    void fetchesOneJobThroughTheProviderDetailsEndpoint() {
        var details = new ReedApiClient(properties).details("reed-123");

        assertThat(requestedUri.get().getPath()).isEqualTo("/jobs/reed-123");
        assertThat(requestedUri.get().getRawQuery()).isNull();
        assertThat(details).isNotNull();
        assertThat(details.getJobDescription())
                .isEqualTo("Complete provider job description");
    }

    @Test
    void rejectsMissingLiveCredentialWithoutMakingARequest() {
        properties.setKey("");

        assertThatThrownBy(() -> search(new ReedApiClient(properties)))
                .isInstanceOf(ReedApiClient.ReedApiException.class)
                .hasMessage(
                        "Reed live provider credential is not configured");
        assertThat(requestedUri).hasValue(null);
    }

    @Test
    void explicitKillSwitchReturnsNoMatchesWithoutCredential() {
        properties.setEnabled(false);
        properties.setKey("");

        var response = search(new ReedApiClient(properties));

        assertThat(response.getTotalResults()).isZero();
        assertThat(response.getResults()).isEmpty();
        assertThat(requestedUri).hasValue(null);
    }

    @Test
    void upstreamFailureLogsNeverContainCredential(
            CapturedOutput output) {
        String privateKey = "private-api-key-for-redaction";
        properties.setKey(privateKey);
        responseStatus.set(400);
        String encodedKey = Base64.getEncoder().encodeToString(
                (privateKey + ":").getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> search(new ReedApiClient(properties)))
                .isInstanceOf(ReedApiClient.ReedApiException.class)
                .hasMessage("Reed API request failed");

        assertThat(output)
                .contains("status=400")
                .doesNotContain(
                        privateKey,
                        encodedKey,
                        HttpHeaders.AUTHORIZATION);
    }

    private com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse
            search(ReedApiClient client) {
        return client.search(
                "Platform Engineer",
                "London",
                25,
                List.of("PERMANENT"),
                null,
                null,
                "GBP",
                1,
                20);
    }

    private void respond(HttpExchange exchange) throws IOException {
        requestedUri.set(exchange.getRequestURI());
        authorization.set(
                exchange.getRequestHeaders().getFirst("Authorization"));
        String responseBody = exchange.getRequestURI().getPath()
                .startsWith("/jobs/")
                ? "{\"jobId\":\"reed-123\",\"jobTitle\":"
                        + "\"Platform Engineer\",\"jobDescription\":"
                        + "\"Complete provider job description\"}"
                : "{\"totalResults\":0,\"results\":[]}";
        byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set(
                "Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus.get(), body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}
