#!/usr/bin/env bash
set -euo pipefail
source test/init
plugin_dir=${YAMLSTAR_TEST_PLUGIN:-}
cli=${YAMLSTAR_TEST_CLI:-$ROOT/cli/bin/yaml}
[[ -n $plugin_dir ]] || plan skip-all 'Set YAMLSTAR_TEST_PLUGIN'
[[ -x $cli ]] || { echo "Native CLI is missing: $cli" >&2; exit 1; }
export YAMLSTAR_LIBRARY_PATH=$plugin_dir
output=$("$cli" --plugin=yaml-parser=rapidyaml --no-install \
  --eval 'a: b # comment')
is "$output" '{"a":"b"}' 'Parser selector loads ABI v2 events'
output=$("$cli" --plugin=yaml-parser=rapidyaml --no-install \
  --eval 'a: &v [1, 2]
b: *v')
is "$output" '{"a":[1,2],"b":[1,2]}' 'Anchors and aliases survive transport'
set +e
output=$("$cli" --plugin=yaml-parser=rapidyaml --no-install \
  --eval '[unterminated' 2>&1)
status=$?
set -e
is "$status" 1 'Malformed YAML fails'
like "$output" 'missing terminating' 'Parser error reaches CLI'
done-testing
