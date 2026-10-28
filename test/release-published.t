#!/usr/bin/env bash

# shellcheck source=test/init
source test/init

temporary=$(mktemp -d)
trap 'rm -rf -- "$temporary"' EXIT
mkdir "$temporary/bin"
cat > "$temporary/bin/curl" <<'BASH'
#!/usr/bin/env bash
url=${!#}
printf '%s\n' "$url" >> "$CURL_LOG"
case $url in
  "https://www.powershellgallery.com/api/v2/Packages(Id='YAMLStar',Version='1.2.3')")
    [[ $* != *-fsIL* ]] || exit 22
    exit 0
    ;;
  "https://www.powershellgallery.com/api/v2/Packages("*)
    exit 22
    ;;
  https://www.powershellgallery.com/packages/YAMLStar/*)
    # Missing Gallery pages redirect to an existing version with HTTP 200.
    exit 0
    ;;
  *) exit 97 ;;
esac
BASH
chmod +x "$temporary/bin/curl"

run-check() {
  if output=$(
    PATH=$temporary/bin:$PATH CURL_LOG=$temporary/curl.log \
      YS_RELEASE_PUBLISHED_QUIET='' \
      bash "$ROOT/util/release-binding-published" powershell "$1" 2>&1
  ); then
    status=0
  else
    status=$?
  fi
}

run-check 1.2.3
ok "$status" 'An existing PowerShell package version is published'
is "$output" 'Published: powershell 1.2.3' \
  'The published status names the requested version'

run-check 9.9.9
is "$status" 1 'A missing version is not published despite Gallery redirects'
is "$output" 'Not published: powershell 9.9.9' \
  'A different published version does not satisfy the requested version'

done-testing
