# Reed Gateway

Reed Gateway isolates Reed API authentication, pagination parameters, and
provider mapping behind the Job Seeker Copilot provider contract. Fixture mode
uses synthetic System Data responses.

Status: **migration candidate; not beta-ready**. It retains the selected useful
legacy history as a sanitised archive tag and records the excluded public ref
explicitly. The System Data client is now generated from a pinned producer
contract. Contract version 1.1 treats a healthy
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

The safe default is `EXTERNAL_PROVIDER_MODE=FIXTURE`, which requires no live
credential and is restricted to non-production use. Enabled `LIVE` mode
requires `REED_API_KEY` before startup succeeds; it has no non-empty repository
default. `REED_API_ENABLED=false` is the provider kill switch. Rotation,
restricted evidence, renewal and incident procedures are defined in
[`docs/CREDENTIAL_OPERATIONS.md`](docs/CREDENTIAL_OPERATIONS.md). The explicit
public-to-sanitised ref decision and no-public-mutation boundary are recorded
in [`docs/HISTORY_SANITISATION.md`](docs/HISTORY_SANITISATION.md).

`develop` is the integration/default branch for beta hardening. See
`CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.
