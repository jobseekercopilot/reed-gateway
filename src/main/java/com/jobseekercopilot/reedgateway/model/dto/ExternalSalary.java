package com.jobseekercopilot.reedgateway.model.dto;

public class ExternalSalary {

    private Integer min;
    private Integer max;
    private String currency;

    public ExternalSalary() {
    }

    public ExternalSalary(Integer min, Integer max, String currency) {
        this.min = min;
        this.max = max;
        this.currency = currency;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}