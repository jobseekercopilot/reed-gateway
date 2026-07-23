# Reed Gateway

Reed Gateway isolates Reed API authentication, pagination parameters, and
provider mapping behind the Job Seeker Copilot provider contract. Fixture mode
uses synthetic System Data responses.

Status: **migration candidate; not beta-ready**. It retains useful legacy and
root-subtree history as archive tags. The System Data client is now generated
from a pinned producer contract; the remaining findings are recorded in
[`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md).

## Local verification

```bash
./scripts/test-contract-policy.sh
./scripts/verify-contracts.sh
mvn -B clean verify
docker build -t local/reed-gateway .
```

Live mode requires `REED_API_KEY`, with no repository default.
`EXTERNAL_PROVIDER_MODE=FIXTURE` is permitted only outside production.

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.
