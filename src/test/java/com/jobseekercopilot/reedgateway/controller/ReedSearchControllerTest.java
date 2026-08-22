package com.jobseekercopilot.reedgateway.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jobseekercopilot.reedgateway.client.ReedApiClient;
import com.jobseekercopilot.reedgateway.client.ReedProviderClient;
import com.jobseekercopilot.reedgateway.exception.GlobalExceptionHandler;
import com.jobseekercopilot.reedgateway.model.dto.ReedJobDto;
import com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReedSearchControllerTest {

    private StubReedProvider provider;
    private ReedSearchController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        provider = new StubReedProvider();
        controller = new ReedSearchController(provider);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsHealthyEmptySearchAsSuccessfulEmptyContract()
            throws Exception {
        ReedSearchResponse providerResponse = new ReedSearchResponse();
        providerResponse.setTotalResults(0);
        providerResponse.setResults(List.of());
        provider.respondWith(providerResponse);

        mvc.perform(validSearch("""
                {
                  "keywords": ["Developer"],
                  "location": "London",
                  "page": 2,
                  "pageSize": 10
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobs").isEmpty())
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void returnsPopulatedSearchAsSuccessfulMappedContract()
            throws Exception {
        ReedJobDto source = new ReedJobDto();
        source.setJobId("reed-1");
        source.setJobTitle("Developer");
        source.setEmployerName("Example Ltd");
        source.setLocationName("London");
        source.setMinimumSalary("45,000");
        source.setMaximumSalary("55,000");
        source.setCurrency("GBP");
        source.setEmploymentType("PERMANENT");
        source.setJobUrl("https://jobs.example.test/reed-1");
        ReedSearchResponse providerResponse = new ReedSearchResponse();
        providerResponse.setTotalResults(1);
        providerResponse.setResults(List.of(source));
        provider.respondWith(providerResponse);

        mvc.perform(validSearch("""
                {
                  "keywords": ["Developer"],
                  "location": "London"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobs[0].id").value("reed-1"))
                .andExpect(jsonPath("$.jobs[0].employmentType")
                        .value("FULL_TIME"))
                .andExpect(jsonPath("$.jobs[0].salary.min")
                        .value(45000))
                .andExpect(jsonPath("$.totalResults").value(1));
    }

    @Test
    void returnsCompleteProviderDetailsForGeneration() throws Exception {
        ReedJobDto source = new ReedJobDto();
        source.setJobId("reed-1");
        source.setJobTitle("Developer");
        source.setEmployerName("Example Ltd");
        source.setJobDescription("Complete responsibilities and requirements.");
        source.setJobUrl("https://jobs.example.test/reed-1");
        provider.respondToDetailsWith(source);

        mvc.perform(get("/api/jobs/reed-1")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("reed-1"))
                .andExpect(jsonPath("$.description")
                        .value("Complete responsibilities and requirements."));
    }

    @Test
    void keepsMissingIdentityDistinctFromEmptySuccess()
            throws Exception {
        mvc.perform(post("/api/jobs/external-search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "keywords": ["Developer"],
                                  "location": "London"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.jobs").isEmpty());
    }

    @Test
    void keepsInvalidRequestDistinctFromEmptySuccess()
            throws Exception {
        mvc.perform(validSearch("""
                {
                  "keywords": [],
                  "location": "London"
                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void keepsUpstreamFailureDistinctFromEmptySuccess()
            throws Exception {
        provider.failWith(new ReedApiClient.ReedApiException(
                "synthetic provider failure"));

        mvc.perform(validSearch("""
                {
                  "keywords": ["Developer"],
                  "location": "London"
                }
                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error")
                        .value("SERVICE_UNAVAILABLE"));
    }

    @Test
    void mapsDecimalAndCommaSeparatedSalariesWithoutDigitConcatenation() {
        assertThat(controller.parseSalary("10.50")).isEqualTo(11);
        assertThat(controller.parseSalary("45,000")).isEqualTo(45000);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
            validSearch(String content) {
        return post("/api/jobs/external-search")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
    }

    private static final class StubReedProvider
            implements ReedProviderClient {

        private ReedSearchResponse response;
        private ReedJobDto details;
        private RuntimeException failure;

        void respondWith(ReedSearchResponse providerResponse) {
            response = providerResponse;
        }

        void failWith(RuntimeException providerFailure) {
            failure = providerFailure;
        }

        void respondToDetailsWith(ReedJobDto providerDetails) {
            details = providerDetails;
        }

        @Override
        public ReedSearchResponse search(
                String keywords,
                String location,
                Integer distance,
                List<String> employmentTypes,
                Integer salaryMin,
                Integer salaryMax,
                String currency,
                Integer page,
                Integer pageSize) {
            if (failure != null) {
                throw failure;
            }
            return response;
        }

        @Override
        public ReedJobDto details(String jobId) {
            if (failure != null) {
                throw failure;
            }
            return details;
        }
    }
}
