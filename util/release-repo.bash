# shellcheck disable=2034,2154

set -euo pipefail

root=$PWD
version=$VERSION

main() (
  init
  clone
  update
  release
)

init() {
  repo_dir=/tmp/yamlstar-$lang
  repo_url=git@github.com:yaml/yamlstar-$lang
  from_dir=$root/$lang
}

clone() (
  rm -fr "$repo_dir"
  git clone "$repo_url" "$repo_dir"
)

release() (
  cd "$repo_dir"
  git add -A .
  git commit -m "Release $version"
  git push
  git tag "$tag_prefix$version"
  git push --tags
)

main "$@"
