package com.jobseekercopilot.reedgateway.controller;

import com.jobseekercopilot.reedgateway.client.ReedApiClient;
import com.jobseekercopilot.reedgateway.model.dto.ExternalSearchRequest;
import com.jobseekercopilot.reedgateway.model.dto.ExternalSearchResponse;
import com.jobseekercopilot.reedgateway.model.dto.ExternalJob;
import com.jobseekercopilot.reedgateway.model.dto.ExternalSalary;
import com.jobseekercopilot.reedgateway.model.dto.ReedJobDto;
import com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class ReedSearchController {

    private final ReedApiClient reedApiClient;

    public ReedSearchController(ReedApiClient reedApiClient) {
        this.reedApiClient = reedApiClient;
    }

    @PostMapping("/external-search")
    public ResponseEntity<ExternalSearchResponse> externalSearch(
            @RequestHeader(name = "X-User-Id", required = false) String userId,
            @Valid @RequestBody ExternalSearchRequest request) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse("UNAUTHORIZED", "Missing X-User-Id header"));
        }

        String keywords = String.join(" ", request.getKeywords());
        String location = request.getLocation();
        Integer distance = request.getDistance();
        List<String> employmentTypes = request.getEmploymentType();
        Integer salaryMin = request.getSalaryMin();
        Integer salaryMax = request.getSalaryMax();
        String currency = request.getCurrency();
        Integer page = request.getPage();
        Integer pageSize = request.getPageSize();

        ReedSearchResponse reedResponse = reedApiClient.search(
                keywords, location, distance, employmentTypes, salaryMin, salaryMax, currency, page, pageSize);

        List<ExternalJob> jobs = new ArrayList<>();
        if (reedResponse.getResults() != null) {
            for (ReedJobDto reedJob : reedResponse.getResults()) {
                jobs.add(toExternalJob(reedJob));
            }
        }

        ExternalSearchResponse response = new ExternalSearchResponse();
        response.setJobs(jobs);
        response.setTotalResults(reedResponse.getTotalResults());
        response.setPage(request.getPage() != null ? request.getPage() : 1);
        response.setPageSize(request.getPageSize() != null ? request.getPageSize() : 20);

        if (jobs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        return ResponseEntity.ok(response);
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
        job.setMatchScore(calculateMatchScore(reedJob));

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

    private Integer parseSalary(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private double calculateMatchScore(ReedJobDto job) {
        return 0.85;
    }

    private ExternalSearchResponse errorResponse(String error, String message) {
        ExternalSearchResponse response = new ExternalSearchResponse();
        response.setJobs(new ArrayList<>());
        response.setTotalResults(0);
        response.setPage(1);
        response.setPageSize(20);
        return response;
    }
}