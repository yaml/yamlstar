#!/usr/bin/env bash

# shellcheck source=test/init
source test/init

runtime=${YS:?Test requires the project YAMLScript runtime}
temporary=$(mktemp -d)
trap 'rm -rf -- "$temporary"' EXIT
mkdir "$temporary/bin"

cat > "$temporary/bin/curl" <<'BASH'
#!/usr/bin/env bash
printf '%s\n' "$*" >> "$CURL_LOG"
if [[ ${BREW_CURL_FAIL:-} ]]; then
  echo 'fixture HTTP 404' >&2
  exit 22
fi
BASH

cat > "$temporary/bin/git" <<'BASH'
#!/usr/bin/env bash
if [[ $* != '-C homebrew-yamlstar status --short'* ]]; then
  echo "Unexpected git command: $*" >&2
  exit 97
fi
BASH
chmod +x "$temporary/bin/"*

run-brew() {
  local mode=$1 directory=$temporary/$1
  mkdir -p "$directory/homebrew-yamlstar"
  for program in yamlstar libyamlstar; do
    for platform in linux-aarch64 linux-x64 macos-arm64 macos-x64; do
      touch "$directory/$program-0.1.28-$platform.tar.xz"
    done
  done
  if output=$(
    cd "$directory" || exit
    export PATH=$temporary/bin:$PATH CURL_LOG=$directory/curl.log
    if [[ $mode == failure ]]; then
      export BREW_CURL_FAIL=1
    fi
    "$runtime" -S "$ROOT/util/brew-update" 0.1.28 2>&1
  ); then
    status=0
  else
    status=$?
  fi
}

run-brew success
ok "$status" 'Homebrew update accepts an existing release'
is "$(< "$temporary/success/curl.log")" \
  '-fsSL -o /dev/null https://github.com/yaml/yamlstar/releases/tag/v0.1.28' \
  'The release check invokes curl with separate arguments'
ok-f "$temporary/success/homebrew-yamlstar/yamlstar.rb" \
  'The CLI formula is generated'
ok-f "$temporary/success/homebrew-yamlstar/libyamlstar.rb" \
  'The library formula is generated'

run-brew failure
is "$status" 1 'Homebrew update rejects a failed release check'
like "$output" 'fixture HTTP 404' 'The curl error is reported'
ok-not-e "$temporary/failure/homebrew-yamlstar/yamlstar.rb" \
  'A failed release check leaves the tap unchanged'

done-testing
