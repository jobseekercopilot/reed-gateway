#!/usr/bin/env bash
set -euo pipefail

contract_dir="${1:-src/main/openapi}"
contract="$contract_dir/system-data-service.json"
manifest="$contract_dir/SHA256SUMS"
source_record="$contract_dir/SOURCE"

for required_file in "$contract" "$manifest" "$source_record"; do
    if [[ ! -f "$required_file" || -L "$required_file" ]]; then
        echo "contract policy: required regular file is missing or is a symlink: $required_file" >&2
        exit 1
    fi
done

(
    cd "$contract_dir"
    sha256sum --check --strict SHA256SUMS
)

test "$(wc -l < "$source_record" | tr -d ' ')" = 4
grep -Fx 'repository=jobseekercopilot/system-data-service' "$source_record" >/dev/null
grep -Fx 'revision=4b5f5460e590c5289cfe1979415ed566e9155e32' "$source_record" >/dev/null
grep -Fx 'path=api/openapi.json' "$source_record" >/dev/null
grep -Fx 'sha256=817afdea8af835e37b1379c47c6730811f3ef5703b29be048b092e676dcc7675' "$source_record" >/dev/null

jq -e '
    (.openapi | type == "string" and startswith("3.")) and
    (.paths["/internal/fixtures/jobs/search"].get.operationId == "searchJobs") and
    (
      [.paths["/internal/fixtures/jobs/search"].get.parameters[].name] ==
      ["datasetId", "datasetVersion", "scenario", "provider", "query", "location", "page", "pageSize", "sort", "salaryMin", "salaryMax", "remoteType"]
    ) and
    (.components.schemas.FixtureJobSearchResponse.properties.jobs.items["$ref"] == "#/components/schemas/DemoJob") and
    (.components.schemas.DemoJob.properties.externalReference.type == "string") and
    (.components.schemas.DemoJob.properties.salaryMinimum.format == "int32")
' "$contract" >/dev/null

echo "contract policy: pinned System Data source is present, intact and compatible"
