package com.jobseekercopilot.reedgateway.model.dto;

import java.util.List;

public class ExternalSearchResponse {

    private List<ExternalJob> jobs;
    private int totalResults;
    private int page;
    private int pageSize;

    public ExternalSearchResponse() {
    }

    public ExternalSearchResponse(List<ExternalJob> jobs, int totalResults, int page, int pageSize) {
        this.jobs = jobs;
        this.totalResults = totalResults;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<ExternalJob> getJobs() {
        return jobs;
    }

    public void setJobs(List<ExternalJob> jobs) {
        this.jobs = jobs;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}