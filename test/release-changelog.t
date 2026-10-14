#!/usr/bin/env bash

# shellcheck source=test/init
source test/init

runtime=${YS:?Test requires the project YAMLScript runtime}
temporary=$(mktemp -d)
trap 'rm -rf -- "$temporary"' EXIT
fixture=$temporary/repo
git init -q "$fixture"
git -C "$fixture" config user.name 'Changelog Test'
git -C "$fixture" config user.email changelog@example.invalid

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

done-testing
