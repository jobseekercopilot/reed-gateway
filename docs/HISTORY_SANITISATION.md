# Reed legacy-history sanitisation decision

## Boundary and decision

The original public repository is
`mcgeeverbernard1992/reed-gateway`. It remains an immutable evidence source for
this migration decision; it is not a deployment or dependency remote for this
private repository. Legacy public repositories remain unchanged.

The public refs were inspected read-only on 2026-07-26. Their content was not
fetched into the private candidate because it includes the exposed
configuration. No force push, public branch update, tag update or deletion is
authorised.

| Original public ref | Original commit | Private candidate outcome |
| --- | --- | --- |
| `refs/heads/main` | `fa617e60b6c8f9538ecd26c88af386d531daeefc` | Sanitised history retained as `refs/tags/archive/legacy/main` at `b40da8789827122cf160098192c2eab045dde6ad`. Author, timestamp and message identify the source commit; the tree intentionally differs because the key default was removed. |
| `refs/heads/master` | `b48dd458a9ed96644106ae0ce708aa7afb489026` | Deliberately not imported. It is a separate public root commit and has no private archive ref. The later public `main` ref was selected as the useful service-history source; importing `master` would reintroduce the exposed configuration without adding candidate lineage. |

The private migration baseline
`c1f90dcfe14360d8eb5b1665ab8009ed135c421e` descends from the sanitised
`archive/legacy/main` commit. This map records exclusion as an explicit outcome
rather than implying that every public object was copied.

## Verification

From a complete authenticated private clone:

```bash
git show-ref
git rev-parse refs/tags/archive/legacy/main
git merge-base --is-ancestor \
  refs/tags/archive/legacy/main \
  refs/heads/develop
./scripts/test-secret-history.sh
./scripts/verify-secret-history.sh
```

Expected invariant:

- the archive tag resolves to the sanitised commit recorded above;
- the tag is an ancestor of `develop`;
- all private candidate refs pass the redacted full-history scan;
- neither original public commit exists in the private object graph; and
- the only configured private remote is `jobseekercopilot/reed-gateway`.

Re-check the public ref SHAs read-only before relying on this mapping in a later
migration. If they have moved, append a reviewed decision; do not rewrite this
record or import exposed objects.

## Credential consequence

History sanitisation reduces redistribution of the exposed value. It cannot
make a public credential secret again. Provider-side revocation and rotation
remain mandatory and are tracked separately from this no-public-mutation
decision.
