package com.jobseekercopilot.reedgateway.model.dto;

public class ReedJobDto {

    private String jobId;
    private String jobTitle;
    private String employerName;
    private String locationName;
    private String minimumSalary;
    private String maximumSalary;
    private String currency;
    private String date;
    private String jobDescription;
    private String jobUrl;
    private String employmentType;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getMinimumSalary() {
        return minimumSalary;
    }

    public void setMinimumSalary(String minimumSalary) {
        this.minimumSalary = minimumSalary;
    }

    public String getMaximumSalary() {
        return maximumSalary;
    }

    public void setMaximumSalary(String maximumSalary) {
        this.maximumSalary = maximumSalary;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }
}