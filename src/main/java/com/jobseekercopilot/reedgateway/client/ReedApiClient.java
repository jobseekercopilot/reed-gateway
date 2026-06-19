package com.jobseekercopilot.reedgateway.client;

import com.jobseekercopilot.reedgateway.config.ReedApiProperties;
import com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class ReedApiClient {

    private final WebClient webClient;
    private final ReedApiProperties properties;

    public ReedApiClient(ReedApiProperties properties) {
        this.properties = properties;
        String auth = properties.getKey() + ":";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ReedSearchResponse search(String keywords, String location, Integer distance, List<String> employmentTypes,
                                     Integer salaryMin, Integer salaryMax, String currency, Integer page, Integer pageSize) {
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
                                builder.queryParam("employmentType", type);
                            }
                        }
                        if (salaryMin != null) {
                            builder.queryParam("minimumSalary", salaryMin);
                        }
                        if (salaryMax != null) {
                            builder.queryParam("maximumSalary", salaryMax);
                        }
                        if (currency != null) {
                            builder.queryParam("currency", currency);
                        }
                        if (page != null) {
                            builder.queryParam("page", page);
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

            return response;
        } catch (WebClientResponseException.TooManyRequests ex) {
            throw new ReedApiException("Rate limit exceeded", ex);
        } catch (WebClientResponseException ex) {
            throw new ReedApiException("Reed API error: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            throw new ReedApiException("Failed to call Reed API: " + ex.getMessage(), ex);
        }
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
        public ReedApiException(String message) {
            super(message);
        }

        public ReedApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}