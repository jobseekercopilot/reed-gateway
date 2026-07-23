package com.jobseekercopilot.reedgateway.controller;

import com.jobseekercopilot.reedgateway.client.ReedProviderClient;
import com.jobseekercopilot.reedgateway.model.dto.ExternalJob;
import com.jobseekercopilot.reedgateway.model.dto.ExternalSalary;
import com.jobseekercopilot.reedgateway.model.dto.ExternalSearchRequest;
import com.jobseekercopilot.reedgateway.model.dto.ExternalSearchResponse;
import com.jobseekercopilot.reedgateway.model.dto.ReedJobDto;
import com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Collections;

@RestController
@RequestMapping("/api")
@Tag(name = "Reed Jobs", description = "Reed job search endpoints")
public class ReedSearchController {

    private final ReedProviderClient reedApiClient;

    public ReedSearchController(ReedProviderClient reedApiClient) {
        this.reedApiClient = reedApiClient;
    }

    @PostMapping("/jobs/external-search")
    @Operation(summary = "Search Reed for the job service", description = "Internal gateway contract used by job-service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "401", description = "Missing X-User-Id header"),
            @ApiResponse(responseCode = "422", description = "Search completed with no matching jobs")
    })
    public ResponseEntity<ExternalSearchResponse> externalSearch(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @Valid @RequestBody ExternalSearchRequest request) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(emptyResponse(request));
        }

        ReedSearchResponse reedResponse = reedApiClient.search(
                String.join(" ", request.getKeywords()), request.getLocation(), request.getDistance(),
                request.getEmploymentType(), request.getSalaryMin(), request.getSalaryMax(),
                request.getCurrency(), request.getPage(), request.getPageSize());

        var jobs = new ArrayList<ExternalJob>();
        if (reedResponse.getResults() != null) {
            reedResponse.getResults().forEach(job -> jobs.add(toExternalJob(job)));
        }

        ExternalSearchResponse response = emptyResponse(request);
        response.setJobs(jobs);
        response.setTotalResults(reedResponse.getTotalResults());
        return jobs.isEmpty() ? ResponseEntity.unprocessableEntity().body(response) : ResponseEntity.ok(response);
    }

    @GetMapping("/reed/search")
    @Operation(summary = "Search jobs on Reed", description = "Searches for jobs via Reed external API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "502", description = "Reed API error or rate limit exceeded")
    })
    @Tag(name = "Reed Jobs")
    public ResponseEntity<ReedSearchResponse> searchJobs(
            @Parameter(description = "Job title or keyword", required = true, example = "Software Engineer")
            @RequestParam String query,
            @Parameter(description = "Location (city or postcode)", required = false, example = "London")
            @RequestParam(required = false) String location,
            @Parameter(description = "Maximum number of results", required = false, example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        ReedSearchResponse response = reedApiClient.search(query, location, null, Collections.emptyList(), null, null, null, null, limit);
        return ResponseEntity.ok(response);
    }

    private ExternalSearchResponse emptyResponse(ExternalSearchRequest request) {
        ExternalSearchResponse response = new ExternalSearchResponse();
        response.setJobs(new ArrayList<>());
        response.setTotalResults(0);
        response.setPage(request.getPage() != null ? request.getPage() : 1);
        response.setPageSize(request.getPageSize() != null ? request.getPageSize() : 20);
        return response;
    }

    private ExternalJob toExternalJob(ReedJobDto reedJob) {
        ExternalJob job = new ExternalJob();
        job.setId(reedJob.getJobId());
        job.setTitle(reedJob.getJobTitle());
        job.setCompany(reedJob.getEmployerName());
        job.setLocation(reedJob.getLocationName());
        job.setEmploymentType(mapEmploymentType(reedJob.getEmploymentType()));
        job.setPostedDate(reedJob.getDate());
        job.setDescription(reedJob.getJobDescription());
        job.setUrl(reedJob.getJobUrl());
        job.setMatchScore(0.85);

        ExternalSalary salary = new ExternalSalary();
        salary.setCurrency(reedJob.getCurrency() != null ? reedJob.getCurrency() : "GBP");
        salary.setMin(parseSalary(reedJob.getMinimumSalary()));
        salary.setMax(parseSalary(reedJob.getMaximumSalary()));
        job.setSalary(salary);
        return job;
    }

    private String mapEmploymentType(String reedType) {
        if (reedType == null) {
            return null;
        }
        return switch (reedType.toUpperCase()) {
            case "PERMANENT" -> "FULL_TIME";
            case "CONTRACT" -> "CONTRACT";
            case "PART_TIME" -> "PART_TIME";
            case "TEMPORARY" -> "TEMPORARY";
            default -> reedType;
        };
    }

    Integer parseSalary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            String normalised = value
                    .replace(",", "")
                    .replaceAll("[^0-9.]", "");
            if (normalised.isBlank()) {
                return null;
            }
            return new BigDecimal(normalised).setScale(0, RoundingMode.HALF_UP).intValue();
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
