package com.jobseekercopilot.reedgateway.client;

import com.jobseekercopilot.reedgateway.config.FixtureProperties;
import com.jobseekercopilot.generated.systemdataservice.api.FixtureControllerApi;
import com.jobseekercopilot.generated.systemdataservice.model.DemoJob;
import com.jobseekercopilot.generated.systemdataservice.model.FixtureJobSearchResponse;
import com.jobseekercopilot.reedgateway.model.dto.ReedJobDto;
import com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "external-provider", name = "mode", havingValue = "FIXTURE")
public class FixtureReedProviderClient implements ReedProviderClient {
    private static final Logger log = LoggerFactory.getLogger(FixtureReedProviderClient.class);
    private final FixtureProperties fixtureProperties;
    private final FixtureControllerApi fixtureControllerApi;

    public FixtureReedProviderClient(FixtureProperties fixtureProperties, FixtureControllerApi fixtureControllerApi) {
        this.fixtureProperties = fixtureProperties;
        this.fixtureControllerApi = fixtureControllerApi;
    }

    @Override
    public ReedSearchResponse search(String keywords, String location, Integer distance, List<String> employmentTypes,
                                     Integer salaryMin, Integer salaryMax, String currency, Integer page, Integer pageSize) {
        delay();
        int safePage = page == null ? 1 : page;
        int safePageSize = pageSize == null ? 20 : pageSize;
        FixtureJobSearchResponse body = fixtureControllerApi.searchJobs(
                fixtureProperties.getDatasetId(),
                fixtureProperties.getDatasetVersion(),
                fixtureProperties.getScenario(),
                null,
                keywords,
                location,
                Math.max(0, safePage - 1),
                safePageSize,
                null,
                salaryMin,
                salaryMax,
                null);
        ReedSearchResponse response = new ReedSearchResponse();
        response.setTotalResults(number(body == null ? null : body.getTotalResults()));
        response.setResults(body == null || body.getJobs() == null ? List.of() : body.getJobs().stream().map(this::toJob).toList());
        log.info("Reed fixture search returned resultCount={} totalResults={} datasetId={} scenario={}",
                response.getResults().size(), response.getTotalResults(), fixtureProperties.getDatasetId(), fixtureProperties.getScenario());
        return response;
    }

    @Override
    public ReedJobDto details(String jobId) {
        delay();
        DemoJob job = fixtureControllerApi.job(
                jobId,
                fixtureProperties.getDatasetId(),
                fixtureProperties.getDatasetVersion());
        return job == null ? null : toJob(job);
    }

    private ReedJobDto toJob(DemoJob source) {
        ReedJobDto job = new ReedJobDto();
        job.setJobId(text(source.getExternalReference(), source.getId()));
        job.setJobTitle(source.getTitle());
        job.setEmployerName(text(source.getCompanyDisplayName(), source.getCompanyName()));
        job.setLocationName(source.getLocationName());
        job.setMinimumSalary(text(source.getSalaryMinimum(), null));
        job.setMaximumSalary(text(source.getSalaryMaximum(), null));
        job.setCurrency(text(source.getSalaryCurrency(), "GBP"));
        job.setDate(source.getDatePosted());
        job.setJobDescription(source.getDescription());
        job.setJobUrl(text(source.getSourceUrl(), "https://fixtures.jobseekercopilot.local/reed/" + job.getJobId()));
        job.setEmploymentType(text(source.getEmploymentType(), source.getContractType()));
        return job;
    }

    private void delay() {
        if (!fixtureProperties.getLatency().isEnabled() || fixtureProperties.getLatency().getJobSearchMs() <= 0) {
            return;
        }
        try {
            Thread.sleep(fixtureProperties.getLatency().getJobSearchMs());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private String text(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }

    private int number(Integer value) {
        return value == null ? 0 : value;
    }
}
