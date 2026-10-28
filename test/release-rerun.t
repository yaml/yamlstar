#!/usr/bin/env bash

# shellcheck source=test/init
source test/init

temporary=$(mktemp -d)
trap 'rm -rf -- "$temporary"' EXIT
fixture=$temporary/repo
git init -q -b release-fixture "$fixture"
git -C "$fixture" config user.name 'Release Test'
git -C "$fixture" config user.email release@example.invalid
printf '%s\n' 'version: 0.1.28' > "$fixture/Meta"
git -C "$fixture" add Meta
git -C "$fixture" commit -q -m 'Create release fixture commit'
git -C "$fixture" tag v0.1.28
original_tag=$(git -C "$fixture" rev-parse v0.1.28)
touch "$fixture/packaging-fix"
git -C "$fixture" add packaging-fix
git -C "$fixture" commit -q -m 'Add committed packaging fixture'

export REAL_GIT GIT_LOG GH_LOG
REAL_GIT=$(command -v git)
GIT_LOG=$temporary/git.log
GH_LOG=$temporary/gh.log
mkdir "$temporary/bin"
cat > "$temporary/bin/git" <<'BASH'
#!/usr/bin/env bash
case $1 in
  push)
    printf '%s\n' "$*" >> "$GIT_LOG"
    [[ $* == 'push origin HEAD:release-fixture' ]] || exit 97
    ;;
  tag)
    echo 'Unexpected release tag change' >&2
    exit 98
    ;;
  *) exec "$REAL_GIT" "$@" ;;
esac
BASH
cat > "$temporary/bin/gh" <<'BASH'
#!/usr/bin/env bash
printf '%s\n' "$*" >> "$GH_LOG"
case "$1 $2" in
  'run view') echo 111 ;;
  'workflow run') echo 'Fixture release dispatched' ;;
  'run list') echo 222 ;;
  'run watch') exit "${WATCH_STATUS:-0}" ;;
  *) exit 99 ;;
esac
BASH
cat > "$temporary/bin/sleep" <<'BASH'
#!/usr/bin/env bash
exit 0
BASH
chmod +x "$temporary/bin/"*

run-rerun() {
  : > "$GIT_LOG"
  : > "$GH_LOG"
  if output=$(
    cd "$fixture" || exit
    PATH=$temporary/bin:$PATH \
      bash "$ROOT/util/make.bash" release-rerun 0.1.28 111 2>&1
  ); then
    status=0
  else
    status=$?
  fi
}

run-rerun
ok "$status" 'Release rerun accepts committed packaging fixes'
is "$(git -C "$fixture" rev-parse v0.1.28)" "$original_tag" \
  'The original release tag is preserved'
is "$(< "$GIT_LOG")" 'push origin HEAD:release-fixture' \
  'Only the current branch is published, without force'
like "$(< "$GH_LOG")" \
  'workflow run release.yaml --repo yaml/yamlstar --ref release-fixture -f version=0.1.28 -f test_artifacts_run_id=111' \
  'The complete release workflow reuses the original artifacts'
like "$(< "$GH_LOG")" 'run watch 222' \
  'The helper watches the dispatched release run'

WATCH_STATUS=42 run-rerun
is "$status" 42 'A failed release workflow propagates its status'

touch "$fixture/uncommitted-fix"
run-rerun
is "$status" 1 'Uncommitted packaging fixes prevent release dispatch'
like "$output" 'requires a clean, committed branch' \
  'The helper explains how to include the pending fixes'
is "$(< "$GH_LOG")" '' 'A dirty branch starts no workflow'
is "$(< "$GIT_LOG")" '' 'A dirty branch publishes no commits'
rm "$fixture/uncommitted-fix"

git -C "$fixture" checkout -q --detach HEAD
run-rerun
is "$status" 1 'A detached checkout prevents release dispatch'
is "$(< "$GH_LOG")" '' 'A detached checkout starts no workflow'

done-testing
