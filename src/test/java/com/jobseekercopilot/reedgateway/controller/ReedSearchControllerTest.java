package com.jobseekercopilot.reedgateway.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReedSearchControllerTest {

    @Test
    void mapsDecimalAndCommaSeparatedSalariesWithoutDigitConcatenation() {
        ReedSearchController controller = new ReedSearchController(null);

        assertThat(controller.parseSalary("10.50")).isEqualTo(11);
        assertThat(controller.parseSalary("45,000")).isEqualTo(45000);
    }
}
