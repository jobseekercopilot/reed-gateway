package com.jobseekercopilot.reedgateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CredentialConfigurationPolicyTest {

    private static final Pattern NON_EMPTY_DEFAULT = Pattern.compile(
            "\\$\\{REED_API_KEY:[^}\\s]+}");

    @Test
    void sourceConfigurationHasOnlyAnEmptyCredentialFallback()
            throws Exception {
        String configuration = Files.readString(
                Path.of("src/main/resources/application.yml"));

        assertThat(configuration)
                .contains(
                        "key: ${REED_API_KEY:}",
                        "mode: ${EXTERNAL_PROVIDER_MODE:FIXTURE}");
        assertThat(NON_EMPTY_DEFAULT.matcher(configuration).find()).isFalse();
    }

    @Test
    void operationsAndHistoryDecisionsRemainDocumented() throws Exception {
        String runbook = Files.readString(
                Path.of("docs/CREDENTIAL_OPERATIONS.md"));
        String history = Files.readString(
                Path.of("docs/HISTORY_SANITISATION.md"));

        assertThat(runbook)
                .contains(
                        "Revoke the previously exposed",
                        "approved secret manager",
                        "restricted security record",
                        "Routine renewal",
                        "Logging and support",
                        "complete authenticated",
                        "clone:",
                        "REED-01 cannot be closed");
        assertThat(history)
                .contains(
                        "fa617e60b6c8f9538ecd26c88af386d531daeefc",
                        "b48dd458a9ed96644106ae0ce708aa7afb489026",
                        "b40da8789827122cf160098192c2eab045dde6ad",
                        "archive/legacy/main",
                        "Deliberately not imported",
                        "No force push",
                        "Legacy public repositories remain unchanged");
    }
}
