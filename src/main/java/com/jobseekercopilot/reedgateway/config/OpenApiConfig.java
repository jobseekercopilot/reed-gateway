package com.jobseekercopilot.reedgateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jobseeker Copilot - Reed Gateway API")
                        .description("Gateway API for Reed job search integration. Searches jobs via Reed external API.")
                        .version("1.0.0"));
    }
}