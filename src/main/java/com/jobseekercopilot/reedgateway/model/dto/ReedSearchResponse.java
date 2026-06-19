package com.jobseekercopilot.reedgateway.model.dto;

import java.util.List;

public class ReedSearchResponse {

    private int totalResults;
    private List<ReedJobDto> results;

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<ReedJobDto> getResults() {
        return results;
    }

    public void setResults(List<ReedJobDto> results) {
        this.results = results;
    }
}