#!/usr/bin/env bash

# shellcheck source=test/init
source test/init

temporary=$(mktemp -d)
trap 'rm -rf -- "$temporary"' EXIT

case $(uname -s)-$(uname -m) in
  Linux-x86_64) platform=linux-x64; extension=so ;;
  Linux-aarch64) platform=linux-aarch64; extension=so ;;
  Darwin-x86_64) platform=macos-x64; extension=dylib ;;
  Darwin-arm64) platform=macos-arm64; extension=dylib ;;
  *) plan skip-all 'Test requires a supported plugin platform' ;;
esac

version=9.8.7
release=yamlstar-plugin-test-plugin-$version-$platform
archive=$temporary/$release.tar.xz
library=libyamlstar-plugin-test-plugin.$extension

mkdir -p "$temporary/release/$release/lib/yamlstar/plugins"
printf 'test plugin\n' > \
  "$temporary/release/$release/lib/yamlstar/plugins/$library"
tar -C "$temporary/release" -cJf "$archive" "$release"

if command -v sha256sum >/dev/null 2>&1; then
  checksum=$(sha256sum "$archive")
else
  checksum=$(shasum -a 256 "$archive")
fi
checksum=${checksum%%[[:space:]]*}
printf '%s  ./%s\n' "$checksum" "${archive##*/}" > \
  "$temporary/SHA256SUMS"

mkdir "$temporary/bin"
cat > "$temporary/bin/curl" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

output=
url=
while (( $# )); do
  case $1 in
    -o)
      output=$2
      shift 2
      ;;
    *)
      url=$1
      shift
      ;;
  esac
done

case $url in
  https://api.github.com/*)
    printf '[{"tag_name": "%s"}]\n' "$FIXTURE_VERSION"
    ;;
  */SHA256SUMS)
    cp "$FIXTURE_CHECKSUMS" "$output"
    ;;
  */*.tar.xz)
    cp "$FIXTURE_ARCHIVE" "$output"
    ;;
  *)
    exit 22
    ;;
esac
EOF
chmod +x "$temporary/bin/curl"

installer=$temporary/prefix/bin/yamlstar-plugin
mkdir -p "${installer%/*}"
cp util/yamlstar-plugin "$installer"

export FIXTURE_VERSION=$version
export FIXTURE_CHECKSUMS=$temporary/SHA256SUMS
export FIXTURE_ARCHIVE=$archive
test_home=$temporary/home

set +e
output=$(env -u YAMLSTAR_LIBRARY_PATH \
  HOME="$test_home" PATH="$temporary/bin:$PATH" \
  "$installer" install test-api test-plugin 2>&1)
status=$?
set -e
ok "$status" 'Installer accepts a valid release archive'
like "$output" 'Installing YAMLStar plugin test-api=test-plugin' \
  'Installer reports progress'

installed=$temporary/prefix/lib/$library
is "$(< "$installed")" 'test plugin' 'Installer writes the expected library'

printf 'not a directory\n' > "$temporary/blocked"
override_dir=$temporary/override/lib
set +e
output=$(HOME="$test_home" PATH="$temporary/bin:$PATH" \
  YAMLSTAR_LIBRARY_PATH="$temporary/blocked/lib:$override_dir" \
  "$installer" install test-api test-plugin 2>&1)
status=$?
set -e
ok "$status" 'Installer selects the first writable plugin path'
override_installed=$override_dir/$library
is "$(< "$override_installed")" 'test plugin' \
  'Installer writes to the selected plugin path'

printf '#!/usr/bin/env bash\nexit 99\n' > "$temporary/bin/curl"
chmod +x "$temporary/bin/curl"
set +e
output=$(env -u YAMLSTAR_LIBRARY_PATH \
  HOME="$test_home" PATH="$temporary/bin:$PATH" \
  "$installer" install test-api test-plugin 2>&1)
status=$?
set -e
ok "$status" 'An installed plugin does not use the network'
is "$output" '' 'An installed plugin produces no progress output'

set +e
output=$(HOME=$test_home util/yamlstar-plugin install '../bad' 2>&1)
status=$?
set -e
not-ok "$status" 'Invalid plugin names are rejected'
like "$output" 'invalid plugin API' 'Invalid plugin errors are specific'

done-testing
