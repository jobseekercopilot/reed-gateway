# Reed Gateway beta-readiness audit

## Blocking findings

- **P0 credential response:** a non-empty Reed key default exists in current
  source and legacy history. The current default is removed here; revoke/rotate
  the key and make an explicit history-sanitisation decision before publishing.
- **P0 provider compliance:** the official developer documentation does not
  establish the required storage, cache, redistribution and attribution rights.
  Written account-specific terms are required.
- **P0 reproducibility:** the System Data generated client is an excluded
  `systemPath` JAR.
- **P1 correctness:** the gateway returns HTTP 422 when Reed returns zero
  matches, even though the provider documents an empty result set. Job Service
  therefore reports a healthy empty provider as unavailable.
- **P1 surface:** the legacy GET search endpoint is an unnecessary internal
  provider surface and has weaker validation.
- **P1 resilience:** retries exist for selected transient failures, but the
  configured timeout values are not applied to the WebClient and blocking calls
  can wait indefinitely.
- **P1 privacy:** query location is logged and needs a data-minimised logging
  policy.
- **P1 coverage/container:** only limited tests exist; the build copies local
  JARs, skips tests in its container build, runs as root, uses unpinned images,
  and lacks a health check.

## Provider evidence

The audit used Reed's official [jobseeker API
documentation](https://www.reed.co.uk/developers/jobseeker) and
[developer portal](https://www.reed.co.uk/developers/). Public documentation
alone is insufficient evidence of redistribution/storage permission.

## Evidence required to close

Clean-clone build/container evidence; versioned contract and drift checks;
credential rotation/full-history decision; written provider permission;
empty/pagination/error/mapping tests; bounded timeouts and retry budgets; and
redacted structured-log verification.

This audit is not a beta-readiness approval.
