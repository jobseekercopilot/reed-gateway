package com.jobseekercopilot.reedgateway.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class ExternalSearchRequest {

    @NotEmpty(message = "keywords must not be empty")
    @Size(min = 1, max = 10, message = "keywords must contain between 1 and 10 items")
    private List<@NotBlank(message = "keyword must not be blank") String> keywords;

    @NotBlank(message = "location must not be blank")
    private String location;

    @Min(value = 1, message = "distance must be at least 1")
    @Max(value = 100, message = "distance must be at most 100")
    private Integer distance;

    private List<String> employmentType;

    @Min(value = 0, message = "salaryMin must be non-negative")
    private Integer salaryMin;

    @Min(value = 0, message = "salaryMax must be non-negative")
    private Integer salaryMax;

    private String currency;

    @Min(value = 1, message = "page must be at least 1")
    private Integer page;

    @Min(value = 1, message = "pageSize must be at least 1")
    @Max(value = 100, message = "pageSize must be at most 100")
    private Integer pageSize;

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public List<String> getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(List<String> employmentType) {
        this.employmentType = employmentType;
    }

    public Integer getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Integer salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Integer getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Integer salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}