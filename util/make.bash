#!/usr/bin/env bash

release-pull() (
  set -ex
  head=$(git rev-parse HEAD)
  git pull --rebase
  if [[ $(git rev-parse HEAD) != "$head" ]]; then
    echo "Pulled new changes. Please re-run 'make release'."
    exit 1
  fi
)

release-tests-retry() (
  set -e
  version=$1
  artifact_run_id=$2
  platforms=$3
  branch=$(git branch --show-current)
  if [[ -z $artifact_run_id ]]; then
    artifact_run_id=$(gh run list --workflow=release.yaml \
      --repo yaml/yamlstar --branch "$branch" --limit=1 \
      --json databaseId --jq '.[0].databaseId')
  fi
  test -n "$artifact_run_id"
  gh run view "$artifact_run_id" --repo yaml/yamlstar \
    --json databaseId --jq .databaseId > /dev/null || {
    echo "ERROR: run id '$artifact_run_id' not found"; exit 1; }
  echo "Using build artifacts from run $artifact_run_id"
  git push origin HEAD:"$branch"
  gh workflow run release.yaml \
    --repo yaml/yamlstar --ref "$branch" -f version="$version" \
    -f tests_only="$platforms" \
    -f test_artifacts_run_id="$artifact_run_id"
  sleep 5
  run_id=$(gh run list --workflow=release.yaml \
    --repo yaml/yamlstar --branch "$branch" --limit=1 \
    --json databaseId --jq '.[0].databaseId')
  gh run watch "$run_id" --repo yaml/yamlstar \
    --exit-status --interval=10
)

release-retry() (
  set -e
  version=$1
  if gh release view "$version" --repo yaml/yamlstar >/dev/null 2>&1; then
    echo "Deleting existing GitHub release $version"
    gh release delete "$version" --repo yaml/yamlstar --yes
  fi
  git push --force-with-lease origin HEAD:"$(git branch --show-current)"
  git tag -f "$version" HEAD
  git tag -f v"$version" HEAD
  git push -f origin "$version" v"$version"
)

release-rerun() (
  set -e
  version=$1
  run_id=$2
  branch=$(git branch --show-current)
  if [[ -z $run_id ]]; then
    run_id=$(gh run list --workflow=release.yaml \
      --repo yaml/yamlstar --branch "$branch" --limit=1 \
      --json databaseId --jq '.[0].databaseId')
  fi
  test -n "$run_id"
  gh run view "$run_id" --repo yaml/yamlstar \
    --json databaseId --jq .databaseId > /dev/null || {
    echo "ERROR: run id '$run_id' not found"; exit 1; }
  git push --force-with-lease origin HEAD:"$branch"
  git tag -f "$version" HEAD
  git tag -f v"$version" HEAD
  git push -f origin "$version" v"$version"
  echo "Rerunning failed jobs of run $run_id"
  gh run rerun "$run_id" --failed --repo yaml/yamlstar
  gh run watch "$run_id" --repo yaml/yamlstar \
    --exit-status --interval=10
)

release-publish-homebrew() (
  set -e
  version=$1
  branch=$(git branch --show-current)
  git push origin HEAD:"$branch"
  gh workflow run release.yaml \
    --repo yaml/yamlstar --ref "$branch" -f version="$version" \
    -f publish_homebrew_only=true
  sleep 5
  run_id=$(gh run list --workflow=release.yaml \
    --repo yaml/yamlstar --branch "$branch" --limit=1 \
    --json databaseId --jq '.[0].databaseId')
  gh run watch "$run_id" --repo yaml/yamlstar \
    --exit-status --interval 10
)

release-publish-bindings() (
  set -e
  version=$1
  force_bindings=$2
  bindings=$3
  bindings_skip=$4
  branch=$(git branch --show-current)
  git push origin HEAD:"$branch"
  gh workflow run release.yaml \
    --repo yaml/yamlstar --ref "$branch" -f version="$version" \
    -f publish_bindings_only=true \
    -f force_bindings="$force_bindings" \
    -f bindings="$bindings" \
    -f bindings_skip="$bindings_skip"
  sleep 5
  run_id=$(gh run list --workflow=release.yaml \
    --repo yaml/yamlstar --branch "$branch" --limit=1 \
    --json databaseId --jq '.[0].databaseId')
  gh run watch "$run_id" --repo yaml/yamlstar \
    --exit-status --interval=10
)

publish-python-wheels() (
  set -e
  version=$1
  rm -fr python/dist
  mkdir -p python/dist/release-assets
  gh release download "$version" \
    --repo yaml/yamlstar \
    --pattern "libyamlstar-$version-*.tar.xz" \
    --dir python/dist/release-assets
)

_prepare-gloat-bin() (
  set -e
  gloat=$1
  gloat_dir=$2
  local_cache=$3
  shift 3
  if ! test -x "$gloat"; then
    case $gloat_dir in
      "$local_cache"/*)
        rm -rf "$gloat_dir"
        "$@" "$gloat"
        ;;
      *)
        echo "Gloat executable not found: $gloat" >&2
        exit 1
        ;;
    esac
  fi
  test -x "$gloat"
)

CLI-DEFAULT-SRC() (
  set -e
  output=$1
  requires=$2
  parser=$3
  mkdir -p "$(dirname "$output")"
  if test -n "$requires"; then
    printf '%s\n' \
      '(ns yamlstar.cli-default' \
      "  (:require $requires))" \
      '' \
      "(def default-parser \"$parser\")" \
      > "$output"
  else
    printf '%s\n' \
      '(ns yamlstar.cli-default)' \
      '' \
      "(def default-parser \"$parser\")" \
      > "$output"
  fi
)

"$@"
