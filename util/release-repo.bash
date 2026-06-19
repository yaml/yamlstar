# shellcheck disable=2034,2154

set -euo pipefail

[[ ${YS_RELEASE_VERBOSE-} ]] && set -x

root=${YAMLSTAR_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd -P)}
version=$YS_RELEASE_VERSION_NEW

main() (
  init
  clone
  update
  if [[ ! ${YS_RELEASE_CI-} ]]; then
    test
  fi
  release
)

git() (
  if [[ ${YS_RELEASE_DRYRUN-} || ${YS_RELEASE_DRY_RUN-} ]]; then
    printf 'X - git'
    printf ' %q' "$@"
    printf '\n'
  else
    command git "$@"
  fi
)

curl() (
  if [[ ${YS_RELEASE_DRYRUN-} || ${YS_RELEASE_DRY_RUN-} ]]; then
    printf 'X - curl'
    printf ' %q' "$@"
    printf '\n'
  else
    command curl "$@"
  fi
)

init() {
  repo_dir=${YS_TMPDIR:-/tmp}/yamlstar-$lang
  repo_url=git@github.com:yaml/yamlstar-$lang
  from_dir=$root/$lang
}

clone() (
  rm -fr "$repo_dir"
  git clone "$repo_url" "$repo_dir"
)

test() (:)

release() (
  cd "$repo_dir" || exit
  git add -A .

  if git diff --cached --quiet; then
    echo "No changes for yamlstar-$lang"
  else
    git commit -m "Release $YS_RELEASE_VERSION_NEW"
    git push origin HEAD
  fi

  tag=$tag_prefix$YS_RELEASE_VERSION_NEW
  if git rev-parse "$tag" >/dev/null 2>&1; then
    git tag -d "$tag"
  fi
  git tag "$tag"
  git push origin "$tag"
)

true
