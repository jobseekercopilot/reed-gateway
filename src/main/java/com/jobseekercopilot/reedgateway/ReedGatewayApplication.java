package com.jobseekercopilot.reedgateway;

import com.jobseekercopilot.reedgateway.config.ReedApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ReedApiProperties.class)
public class ReedGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReedGatewayApplication.class, args);
    }
}