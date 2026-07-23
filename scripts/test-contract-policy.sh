#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
temporary_dir="$(mktemp -d)"
trap 'rm -rf "$temporary_dir"' EXIT

copy_contract() {
    local destination="$1"
    mkdir -p "$destination"
    cp "$repository_root/src/main/openapi/system-data-service.json" \
       "$repository_root/src/main/openapi/SHA256SUMS" \
       "$repository_root/src/main/openapi/SOURCE" \
       "$destination/"
}

"$repository_root/scripts/verify-contracts.sh" "$repository_root/src/main/openapi" >/dev/null

copy_contract "$temporary_dir/missing"
rm "$temporary_dir/missing/system-data-service.json"
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/missing" >/dev/null 2>&1; then
    echo "contract policy negative test accepted a missing contract" >&2
    exit 1
fi

copy_contract "$temporary_dir/drift"
jq '.info.description = "unreviewed drift"' "$temporary_dir/drift/system-data-service.json" > "$temporary_dir/drift/changed.json"
mv "$temporary_dir/drift/changed.json" "$temporary_dir/drift/system-data-service.json"
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/drift" >/dev/null 2>&1; then
    echo "contract policy negative test accepted checksum drift" >&2
    exit 1
fi

copy_contract "$temporary_dir/operation"
jq 'del(.paths["/internal/fixtures/jobs/search"].get)' "$temporary_dir/operation/system-data-service.json" > "$temporary_dir/operation/changed.json"
mv "$temporary_dir/operation/changed.json" "$temporary_dir/operation/system-data-service.json"
(cd "$temporary_dir/operation" && sha256sum system-data-service.json > SHA256SUMS)
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/operation" >/dev/null 2>&1; then
    echo "contract policy negative test accepted removal of fixture job search" >&2
    exit 1
fi

echo "contract policy tests passed"
