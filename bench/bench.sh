#!/usr/bin/env bash

set -euo pipefail

cat >&2 <<'EOF'
bench/bench.sh is currently unsupported.

The previous benchmark path compiled YAMLStar and the vendored parser sources
through Gloat. YAMLStar now uses org.yamlstar/yaml-parser as a Maven dependency,
and the Gloat source-list build does not consume that dependency yet.
EOF

exit 1
