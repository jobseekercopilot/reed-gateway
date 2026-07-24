# Reed Gateway

Reed Gateway isolates Reed API authentication, pagination parameters, and
provider mapping behind the Job Seeker Copilot provider contract. Fixture mode
uses synthetic System Data responses.

Status: **migration candidate; not beta-ready**. It retains useful legacy and
root-subtree history as archive tags. The System Data client is now generated
from a pinned producer contract. Contract version 1.1 treats a healthy
zero-result search as `200` with an empty `jobs` collection and truthful
pagination metadata; invalid, unauthenticated and upstream-failure responses
remain distinct. The remaining findings are recorded in
[`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md).

Its provider-specific ownership and the boundary with canonical Job Service
results are defined in the Infrastructure
[Job Search architecture ADR](https://github.com/jobseekercopilot/infrastructure/blob/develop/docs/adr/0001-job-search-architecture-and-ownership.md).

## Local verification

```bash
./scripts/test-contract-policy.sh
./scripts/verify-contracts.sh
mvn -B clean verify
docker build -t local/reed-gateway .
```

The controller regression suite uses only a deterministic provider stub. It verifies
populated and empty success responses, request validation, missing caller
identity, upstream failure translation and salary parsing without a live Reed
call.

Live mode requires `REED_API_KEY`, with no repository default.
`EXTERNAL_PROVIDER_MODE=FIXTURE` is permitted only outside production.

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.
