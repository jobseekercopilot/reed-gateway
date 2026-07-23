package com.jobseekercopilot.reedgateway.client;

import com.jobseekercopilot.reedgateway.model.dto.ReedSearchResponse;
import java.util.List;

public interface ReedProviderClient {
    ReedSearchResponse search(String keywords, String location, Integer distance, List<String> employmentTypes,
                              Integer salaryMin, Integer salaryMax, String currency, Integer page, Integer pageSize);
}
