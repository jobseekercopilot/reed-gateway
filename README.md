# Reed Gateway

Reed Gateway isolates Reed API authentication, pagination parameters, and
provider mapping behind the Job Seeker Copilot provider contract. Fixture mode
uses synthetic System Data responses.

Status: **migration candidate; not beta-ready**. It retains useful legacy and
root-subtree history as archive tags, but the active source cannot build
without an excluded local System Data client JAR. See
[`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md).

## Local verification

```bash
mvn -B clean verify
docker build -t local/reed-gateway .
```

Live mode requires `REED_API_KEY`, with no repository default.
`EXTERNAL_PROVIDER_MODE=FIXTURE` is permitted only outside production.

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.
