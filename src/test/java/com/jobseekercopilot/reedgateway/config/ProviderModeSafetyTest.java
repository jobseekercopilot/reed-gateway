package com.jobseekercopilot.reedgateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(OutputCaptureExtension.class)
class ProviderModeSafetyTest {

    @Test
    void fixtureIsTheSafeDefaultAndNeedsNoLiveCredential() {
        ExternalProviderProperties provider =
                new ExternalProviderProperties();
        ReedApiProperties reed = reed(true, "");

        assertThat(provider.getMode()).isEqualTo(ExternalProviderMode.FIXTURE);
        assertThatCode(() -> safety(provider, reed, new MockEnvironment())
                        .run(null))
                .doesNotThrowAnyException();
    }

    @Test
    void enabledLiveModeFailsClosedWhenCredentialIsMissing() {
        assertThatThrownBy(() -> safety(
                                provider(ExternalProviderMode.LIVE),
                                reed(true, ""),
                                new MockEnvironment())
                        .run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Reed LIVE mode requires REED_API_KEY.");
    }

    @Test
    void configuredLiveModeLogsOnlyCredentialState(
            CapturedOutput output) {
        String privateKey = "private-live-api-key";

        safety(
                        provider(ExternalProviderMode.LIVE),
                        reed(true, privateKey),
                        new MockEnvironment())
                .run(null);

        assertThat(output)
                .contains(
                        "mode=LIVE",
                        "externalCallsEnabled=true",
                        "credentialConfigured=true")
                .doesNotContain(privateKey);
    }

    @Test
    void disabledLiveProviderIsAnExplicitSafeKillSwitch() {
        assertThatCode(() -> safety(
                                provider(ExternalProviderMode.LIVE),
                                reed(false, ""),
                                new MockEnvironment())
                        .run(null))
                .doesNotThrowAnyException();
    }

    @Test
    void productionStillRejectsFixtureMode() {
        MockEnvironment environment =
                new MockEnvironment().withProperty(
                        "spring.profiles.active", "production");
        environment.setActiveProfiles("production");

        assertThatThrownBy(() -> safety(
                                provider(ExternalProviderMode.FIXTURE),
                                reed(true, ""),
                                environment)
                        .run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "cannot start in FIXTURE mode with a production profile");
    }

    private ProviderModeSafety safety(
            ExternalProviderProperties provider,
            ReedApiProperties reed,
            MockEnvironment environment) {
        FixtureProperties fixture = new FixtureProperties();
        fixture.setDatasetId("synthetic-dataset");
        fixture.setDatasetVersion("1.0");
        fixture.setScenario("DEMO_READY");
        return new ProviderModeSafety(
                provider, reed, fixture, environment);
    }

    private ExternalProviderProperties provider(ExternalProviderMode mode) {
        ExternalProviderProperties provider =
                new ExternalProviderProperties();
        provider.setMode(mode);
        return provider;
    }

    private ReedApiProperties reed(
            boolean enabled, String apiKey) {
        ReedApiProperties reed = new ReedApiProperties();
        reed.setEnabled(enabled);
        reed.setKey(apiKey);
        return reed;
    }
}
