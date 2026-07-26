# Reed credential operations

## Security boundary

Reed Gateway has no live credential in candidate source, examples, images or
fixture configuration. It starts in `FIXTURE` mode by default. A production
profile rejects fixture mode, and enabled `LIVE` mode requires `REED_API_KEY`
before startup completes.

`REED_API_ENABLED=false` is the emergency kill switch. It prevents provider
requests and returns an empty provider result. It is not evidence that an
exposed credential has been revoked.

The live value must be injected from the approved deployment secret store into
the environment variable. Do not place it in Git, GitHub, Compose files, issue
comments, pull requests, build arguments, container images, shell history,
support tickets, dashboards or log configuration.

## Initial exposure response

The repository cannot revoke or rotate a Reed account credential. The provider
account administrator must:

1. Disable Reed traffic with `REED_API_ENABLED=false`.
2. Revoke the previously exposed API key in the provider account before
   relying on source or candidate-history cleanup.
3. Create a replacement under the approved organisation account and
   least-privilege product/usage settings.
4. Store the replacement only in the approved secret manager.
5. Update the deployment's secret reference without copying the value into an
   issue, command line or deployment manifest.
6. Start one approved environment with `EXTERNAL_PROVIDER_MODE=LIVE` and the
   injected variable, then verify health and a bounded synthetic request.
7. Confirm the old key is rejected and the replacement or its Basic
   Authorization encoding is absent from application, platform, proxy and
   provider-support logs.
8. Re-enable traffic only after account ownership, terms, attribution, quota,
   retention and cost approvals are recorded.

Keep the following evidence in the restricted security record, not in this
repository:

- rotation UTC date/time and named account administrator;
- provider account/application reference without the credential value;
- revocation and replacement confirmation;
- secret-manager reference/version without secret content;
- affected environments and deployment revision;
- redacted verification result; and
- incident owner, review outcome and next renewal date.

## Routine renewal

Rotate on the organisation schedule and immediately after suspected exposure,
provider-account ownership changes or access-control incidents. Use an overlap
only if the provider supports two independently revocable keys and the security
owner approves it. Revoke the former key after the replacement is healthy,
then remove its secret version according to the secret-manager retention
policy.

## Logging and support

The Reed credential is encoded into an HTTP Basic Authorization header. Never
log the key, encoded header, `WebClientResponseException`, request object,
Reactor Netty wiretap or HTTP-client debug output in LIVE mode. Application
provider failures log only fixed operation text, HTTP status, duration and
exception class.

Do not paste an exception, request or provider dashboard screenshot into a
ticket until credentials and account identifiers have been removed. If a
credential reaches a log or support channel, disable traffic, revoke it and
follow the incident steps above.

## Repository and history verification

Use the repository-owned fail-closed controls from a complete authenticated
clone:

```bash
./scripts/test-secret-history.sh
./scripts/verify-secret-history.sh
```

The verification refuses shallow or empty history and scans all reachable
candidate commits and archive tags. Keep scanner version, complete-history
proof, commit SHA, UTC time and pass/fail result. Do not retain a matching
secret value in the evidence.

Also verify:

```bash
rg -n 'REED_API_KEY' --glob '!target/**'
mvn -B --no-transfer-progress clean verify
```

The only application configuration form allowed in candidate source is:

```yaml
key: ${REED_API_KEY:}
```

See [`HISTORY_SANITISATION.md`](HISTORY_SANITISATION.md) for the immutable
public-to-private ref decision. A clean private candidate does not revoke the
credential already exposed in public history.

## Remaining external closure evidence

REED-01 cannot be closed from repository work alone. Closure requires the
restricted revocation/rotation evidence from the provider account
administrator. Repository comments must record only that the evidence was
reviewed, by whom and when—never the value.
