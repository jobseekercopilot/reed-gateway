# Pinned System Data contract

`system-data-service.json` is an exact vendored copy of the producer-owned
`jobseekercopilot/system-data-service/api/openapi.json` at the revision recorded
in `SOURCE`. `SHA256SUMS` protects the reviewed bytes.

To update it, merge and verify the producer contract first, copy the exact
source document, update `SOURCE` and `SHA256SUMS`, then run
`scripts/test-contract-policy.sh`, `scripts/verify-contracts.sh` and
`mvn -B clean verify`. Never edit the vendored copy independently.

OpenAPI Generator 7.5.0 creates the RestTemplate client during Maven
`generate-sources`; no generated JAR or source is committed.
