package com.jobseekercopilot.reedgateway.client;

import com.jobseekercopilot.reedgateway.config.ReedApiProperties;
import com.jobseekercopilot.reedgateway.logging.CorrelationIdFilter;
import com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse;
import com.jobseekercopilot.reedgateway.model.dto.ReedJobDto;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "LIVE", matchIfMissing = true)
public class ReedApiClient implements ReedProviderClient {

    private static final Logger log = LoggerFactory.getLogger(ReedApiClient.class);

    private final WebClient webClient;
    private final ReedApiProperties properties;

    public ReedApiClient(ReedApiProperties properties) {
        this.properties = properties;
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE);
        if (!blank(properties.getKey())) {
            String auth = properties.getKey() + ":";
            String encodedAuth = Base64.getEncoder().encodeToString(
                    auth.getBytes(StandardCharsets.UTF_8));
            builder.defaultHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Basic " + encodedAuth);
        }
        this.webClient = builder
                .filter((request, next) -> {
                    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
                    if (StringUtils.hasText(correlationId)) {
                        return next.exchange(ClientRequest.from(request)
                                .header(CorrelationIdFilter.HEADER_NAME, correlationId)
                                .build());
                    }
                    return next.exchange(request);
                })
                .build();
    }

    @Override
    public ReedSearchResponse search(String keywords, String location, Integer distance, List<String> employmentTypes,
                                     Integer salaryMin, Integer salaryMax, String currency, Integer page, Integer pageSize) {
        if (!properties.isEnabled()) {
            log.warn("Reed provider is disabled");
            return empty();
        }
        if (blank(properties.getKey())) {
            throw new ReedApiException(
                    "Reed live provider credential is not configured");
        }
        long startedAt = System.nanoTime();
        log.info("Reed provider request started hasKeywords={} location={} distance={} page={} pageSize={} employmentTypes={}",
                keywords != null && !keywords.isBlank(),
                location,
                distance,
                page,
                pageSize,
                employmentTypes == null ? 0 : employmentTypes.size());
        try {
            ReedSearchResponse response = webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(properties.getSearchPath());
                        builder.queryParam("keywords", keywords);
                        builder.queryParam("locationName", location);
                        if (distance != null) {
                            builder.queryParam("distanceFromLocation", distance);
                        }
                        if (employmentTypes != null && !employmentTypes.isEmpty()) {
                            for (String type : employmentTypes) {
                                switch (type.trim().toUpperCase(Locale.ROOT)) {
                                    case "FULL_TIME" ->
                                            builder.queryParam("fullTime", true);
                                    case "PART_TIME" ->
                                            builder.queryParam("partTime", true);
                                    case "CONTRACT" ->
                                            builder.queryParam("contract", true);
                                    case "TEMPORARY", "TEMP" ->
                                            builder.queryParam("temp", true);
                                    case "PERMANENT" ->
                                            builder.queryParam("permanent", true);
                                    default -> {
                                        // Unsupported values are not sent to Reed.
                                    }
                                }
                            }
                        }
                        if (salaryMin != null) {
                            builder.queryParam("minimumSalary", salaryMin);
                        }
                        if (salaryMax != null) {
                            builder.queryParam("maximumSalary", salaryMax);
                        }
                        if (page != null && pageSize != null) {
                            builder.queryParam(
                                    "resultsToSkip",
                                    Math.max(0, page - 1) * pageSize);
                        }
                        if (pageSize != null) {
                            builder.queryParam("resultsToTake", pageSize);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(ReedSearchResponse.class)
                    .retryWhen(Retry.backoff(3, java.time.Duration.ofSeconds(1))
                            .maxBackoff(java.time.Duration.ofSeconds(4))
                            .filter(this::isRetryable))
                    .block();

            int resultCount = response == null || response.getResults() == null ? 0 : response.getResults().size();
            int totalResults = response == null ? 0 : response.getTotalResults();
            log.info("Reed provider returned status=200 resultCount={} totalResults={} durationMs={}",
                    resultCount,
                    totalResults,
                    (System.nanoTime() - startedAt) / 1_000_000);
            return response;
        } catch (WebClientResponseException.TooManyRequests ex) {
            log.warn("Reed provider rate limited status={} durationMs={}",
                    ex.getStatusCode().value(),
                    (System.nanoTime() - startedAt) / 1_000_000);
            throw new ReedApiException(
                    "Reed rate limit exceeded",
                    HttpStatus.TOO_MANY_REQUESTS);
        } catch (WebClientResponseException ex) {
            log.warn("Reed provider failed status={} durationMs={} error={}",
                    ex.getStatusCode().value(),
                    (System.nanoTime() - startedAt) / 1_000_000,
                    ex.getClass().getSimpleName());
            HttpStatus status = ex.getStatusCode() == HttpStatus.UNAUTHORIZED
                    || ex.getStatusCode() == HttpStatus.FORBIDDEN
                    ? HttpStatus.valueOf(ex.getStatusCode().value())
                    : HttpStatus.SERVICE_UNAVAILABLE;
            throw new ReedApiException(
                    "Reed API request failed",
                    status);
        } catch (Exception ex) {
            log.warn("Reed provider failed durationMs={} error={}",
                    (System.nanoTime() - startedAt) / 1_000_000,
                    ex.getClass().getSimpleName());
            throw new ReedApiException(
                    "Reed API request failed",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public ReedJobDto details(String jobId) {
        if (!properties.isEnabled()) {
            log.warn("Reed provider is disabled");
            return null;
        }
        if (blank(properties.getKey())) {
            throw new ReedApiException(
                    "Reed live provider credential is not configured");
        }
        if (blank(jobId)) {
            throw new ReedApiException("Reed job ID is required");
        }
        long startedAt = System.nanoTime();
        log.info("Reed provider detail request started");
        try {
            ReedJobDto response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/jobs/{jobId}")
                            .build(jobId))
                    .retrieve()
                    .bodyToMono(ReedJobDto.class)
                    .block();
            log.info(
                    "Reed provider detail returned status=200 hasDescription={} durationMs={}",
                    response != null && !blank(response.getJobDescription()),
                    (System.nanoTime() - startedAt) / 1_000_000);
            return response;
        } catch (WebClientResponseException exception) {
            log.warn(
                    "Reed provider detail failed status={} durationMs={} error={}",
                    exception.getStatusCode().value(),
                    (System.nanoTime() - startedAt) / 1_000_000,
                    exception.getClass().getSimpleName());
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            HttpStatus status = exception.getStatusCode()
                    == HttpStatus.TOO_MANY_REQUESTS
                    ? HttpStatus.TOO_MANY_REQUESTS
                    : HttpStatus.SERVICE_UNAVAILABLE;
            throw new ReedApiException("Reed API request failed", status);
        } catch (RuntimeException exception) {
            log.warn(
                    "Reed provider detail failed durationMs={} error={}",
                    (System.nanoTime() - startedAt) / 1_000_000,
                    exception.getClass().getSimpleName());
            throw new ReedApiException(
                    "Reed API request failed",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private ReedSearchResponse empty() {
        ReedSearchResponse response = new ReedSearchResponse();
        response.setTotalResults(0);
        response.setResults(List.of());
        return response;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isRetryable(Throwable error) {
        if (error instanceof WebClientResponseException ex) {
            return ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR ||
                   ex.getStatusCode() == HttpStatus.BAD_GATEWAY ||
                   ex.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE;
        }
        return error instanceof java.io.IOException;
    }

    public static class ReedApiException extends RuntimeException {
        private final HttpStatus status;

        public ReedApiException(String message) {
            this(message, HttpStatus.SERVICE_UNAVAILABLE);
        }

        public ReedApiException(String message, HttpStatus status) {
            super(message, null, false, false);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
}
