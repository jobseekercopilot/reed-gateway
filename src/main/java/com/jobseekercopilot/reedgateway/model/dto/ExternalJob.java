package com.jobseekercopilot.reedgateway.model.dto;

public class ExternalJob {

    private String id;
    private String title;
    private String company;
    private String location;
    private ExternalSalary salary;
    private String employmentType;
    private String postedDate;
    private String description;
    private String url;
    private double matchScore;

    public ExternalJob() {
    }

    public ExternalJob(String id, String title, String company, String location, ExternalSalary salary, String employmentType, String postedDate, String description, String url, double matchScore) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.employmentType = employmentType;
        this.postedDate = postedDate;
        this.description = description;
        this.url = url;
        this.matchScore = matchScore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ExternalSalary getSalary() {
        return salary;
    }

    public void setSalary(ExternalSalary salary) {
        this.salary = salary;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(String postedDate) {
        this.postedDate = postedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }
}