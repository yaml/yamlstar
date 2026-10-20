#!/usr/bin/env bash

# shellcheck source=test/init
source test/init

runtime=${YS:?Test requires the project YAMLScript runtime}
release_script=$ROOT/util/release-yamlstar
temporary=$(mktemp -d)
trap 'rm -rf -- "$temporary"' EXIT
fixture=$temporary/repo
git init -q "$fixture"
git -C "$fixture" config user.name 'Changelog Test'
git -C "$fixture" config user.email changelog@example.invalid
printf '%s\n' 'version: 0.1.23' > "$fixture/Meta"
git -C "$fixture" add Meta

commit() {
  git -C "$fixture" commit -q --allow-empty -m "$1"
}

commit 'Initial legacy release'
initial=$(git -C "$fixture" rev-parse HEAD)
git -C "$fixture" tag 0.1.22
commit 'First changelog improvement'
git -C "$fixture" tag v0.1.23
commit 'Second changelog improvement'
commit 'Third changelog improvement'

reset-changes() {
  printf '%s\n' '- version: 0.1.22' '  changes:' \
    '  - Initial legacy release' > "$fixture/Changes"
  rm -f "$fixture/release-changes.txt.0.1.24"
}

run-changelog() {
  local implementation=$1 old=$2
  if output=$(
    exec 2>&1
    cd "$fixture" || exit
    export EDITOR=true GH_TOKEN=fixture YS=$runtime
    if [[ $implementation == yamlscript ]]; then
      "$runtime" -S "$ROOT/util/release-yamlstar" \
        changelog "$old" 0.1.24 <<< ''
    else
      OLD_VERSION=$old NEW_VERSION=0.1.24 \
        bash "$ROOT/util/release" changelog
    fi
  ); then
    status=0
  else
    status=$?
  fi
}

expected=$'  - Second changelog improvement\n  - Third changelog improvement'

run-sanity-check() {
  if output=$(
    cd "$fixture" || exit
    "$runtime" -S "$ROOT/util/release-yamlstar" \
      sanity-check "$1" "$2" 2>&1
  ); then
    status=0
  else
    status=$?
  fi
}

run-sanity-check 0.1.23 0.1.24
ok "$status" 'YAMLScript reads the current version for release checks'
like "$output" 'Old version matches current: 0.1.23' \
  'The sanity check receives the version from Meta'

run-sanity-check 0.1.22 0.1.24
is "$status" 1 'YAMLScript rejects the wrong old version'
like "$output" 'Current version: 0.1.23' \
  'A version mismatch reports the actual current version'

run-sanity-check 0.1.23 0.1.23
is "$status" 1 'YAMLScript rejects an existing release tag'

for implementation in yamlscript bash; do
  reset-changes
  run-changelog "$implementation" 0.1.23
  ok "$status" "$implementation accepts a v-prefixed release tag"
  like "$(< "$fixture/Changes")" "$expected" \
    "$implementation preserves chronological commit order"

  reset-changes
  run-changelog "$implementation" 0.1.22
  ok "$status" "$implementation accepts a legacy release tag"
  like "$(< "$fixture/Changes")" 'First changelog improvement' \
    "$implementation includes commits after the legacy release"

  reset-changes
  before=$(< "$fixture/Changes")
  run-changelog "$implementation" 0.1.21
  is "$status" 1 "$implementation rejects a missing release tag"
  like "$output" 'Cannot find release tag' \
    "$implementation reports the missing tag"
  is "$(< "$fixture/Changes")" "$before" \
    "$implementation leaves Changes intact when generation fails"
done

git -C "$fixture" tag 0.1.23 "$initial"
for implementation in yamlscript bash; do
  reset-changes
  run-changelog "$implementation" 0.1.23
  ok "$status" "$implementation accepts both release tag forms"
  like "$(< "$fixture/Changes")" "$expected" \
    "$implementation prefers the v-prefixed tag"
  unlike "$(< "$fixture/Changes")" 'First changelog improvement' \
    "$implementation excludes commits before the preferred tag"
done

export REAL_GIT
REAL_GIT=$(command -v git)
mkdir "$temporary/bin"
cat > "$temporary/bin/git" <<'EOF'
#!/usr/bin/env bash
if [[ $1 == log ]]; then
  echo 'Fixture git log failure' >&2
  exit 42
fi
exec "$REAL_GIT" "$@"
EOF
chmod +x "$temporary/bin/git"

for implementation in yamlscript bash; do
  reset-changes
  before=$(< "$fixture/Changes")
  PATH=$temporary/bin:$PATH run-changelog "$implementation" 0.1.23
  is "$status" 1 "$implementation rejects a failed Git history read"
  like "$output" 'Fixture git log failure' \
    "$implementation reports the Git history error"
  is "$(< "$fixture/Changes")" "$before" \
    "$implementation leaves Changes intact when Git log fails"
done

reset-changes
touch "$fixture/release-changes.txt.0.1.24"
run-changelog yamlscript 0.1.23
ok "$status" 'YAMLScript regenerates an empty cached changelog'
is "$(< "$fixture/release-changes.txt.0.1.24")" "$expected" \
  'The regenerated changelog contains the expected commits'

reset-changes
printf '%s\n' '  - Keep the reviewed changelog' > \
  "$fixture/release-changes.txt.0.1.24"
run-changelog yamlscript 0.1.23
ok "$status" 'YAMLScript accepts a reviewed cached changelog'
is "$(< "$fixture/release-changes.txt.0.1.24")" \
  '  - Keep the reviewed changelog' 'Reviewed changelog text is preserved'

mkdir -p "$fixture/util" "$temporary/home"
cat > "$temporary/home/.yamlstar-secrets.yaml" <<'EOF'
github:
  token: fixture-token
EOF
cat > "$fixture/util/version-bump" <<'EOF'
!ys-0
write 'release-env':
  str: ENV.ROOT "\n" ENV.GH_TOKEN "\n"
say: 'Fixture version bump'
EOF

run-version-bump() {
  if output=$(
    cd "$fixture" || exit
    env -u GH_TOKEN -u GITHUB_TOKEN HOME="$temporary/home" ROOT=unexpected \
      YS="$runtime" "$runtime" -S "$release_script" \
      version-bump <<< '' 2>&1
  ); then
    status=0
  else
    status=$?
  fi
}

run-version-bump
ok "$status" 'The release helper runs child commands with YAMLScript'
is "$(< "$fixture/release-env")" "$fixture"$'\nfixture-token' \
  'Child commands receive ROOT and the token from the secrets file'
like "$output" 'Fixture version bump' \
  'The release helper displays child command output'

cat > "$fixture/util/version-bump" <<'EOF'
!ys-0
die: 'Fixture command failure'
EOF
run-version-bump
is "$status" 1 'The release helper stops when a child command fails'
like "$output" 'Fixture command failure' \
  'The release helper displays the child command error'

done-testing
