#!/usr/bin/env bash

# shellcheck source=test/init
source test/init

cli=${YAMLSTAR_TEST_CLI:-$ROOT/cli/bin/yaml}
[[ -x $cli ]] || plan skip-all 'Build the native CLI with make cli'

load_alias() {
  env YAMLSTAR_CONFIG= "$cli" --from=yaml --no-install \
    --plugin=yaml-parser="$parser" \
    --config="{plugin: {alias-data: $1}}" -j --eval "$2"
}

for parser in go-yaml reference json-comments; do
  output=$(load_alias '{path: true}' $'base: &x 42\ncopy: *x\n')
  is "$output" '{"base":42,"copy":42}' \
    "$parser preserves plain aliases in path mode"

  output=$(load_alias '{path: false}' \
    $'base: &x.foo found\ncopy: *x.foo\n')
  is "$output" '{"base":"found","copy":"found"}' \
    "$parser preserves literal dotted names when paths are disabled"

  output=$(load_alias '{path: true}' \
    $'base: &x [zero, {baz: 42}]\ncopy: *x.1.baz\n')
  is "$output" '{"base":["zero",{"baz":42}],"copy":42}' \
    "$parser traverses a sequence and mapping"

  output=$(load_alias '{path: true}' \
    $'base: &foo {\'42\': {bar: 42}, \'042\': leading}\ncopy: *foo.\'42\'.bar\nleading: *foo.\'042\'\n')
  is "$output" \
    '{"base":{"42":{"bar":42},"042":"leading"},"copy":42,"leading":"leading"}' \
    "$parser selects quoted numeric string keys without normalizing zeros"

  output=$(load_alias '{path: true}' \
    $'base: &foo [{\'42\': {bar: 42}}]\ncopy: *foo.0.\'42\'.bar\n')
  is "$output" '{"base":[{"42":{"bar":42}}],"copy":42}' \
    "$parser distinguishes sequence indices from quoted mapping keys"

  for alias in foo.00 foo.01 foo.042; do
    set +e
    output=$(load_alias '{path: true, data: {foo: [found]}}' \
      "copy: *$alias" 2>&1)
    status=$?
    set -e
    is "$status" 1 "$parser rejects leading zeros in $alias"
    like "$output" 'sequence index must not have leading zeros' \
      "$parser reports leading zeros in $alias"
  done

  for alias in "foo.'word'" "foo.'-42'" "foo.'+42'" "foo.'4.2'" \
    'foo."42"' "foo.'42" "foo.42'" foo.\<\< bad/name.foo; do
    set +e
    output=$(load_alias '{path: true}' "copy: *$alias" 2>&1)
    status=$?
    set -e
    is "$status" 1 "$parser rejects invalid path $alias"
    like "$output" 'invalid path segment' \
      "$parser reports invalid syntax in $alias"
  done

  set +e
  output=$(load_alias "{path: true, data: {foo: {'42': found}}}" \
    'copy: *foo.42' 2>&1)
  status=$?
  set -e
  is "$status" 1 "$parser rejects unquoted numeric mapping keys"
  like "$output" 'numeric mapping keys must be single-quoted' \
    "$parser explains how to quote a numeric mapping key"

  set +e
  output=$(load_alias '{path: true, data: {foo: [found]}}' \
    "copy: *foo.'0'" 2>&1)
  status=$?
  set -e
  is "$status" 1 "$parser rejects quoted sequence indices"
  like "$output" 'sequence index must contain decimal digits' \
    "$parser requires bare digits for a sequence index"

  output=$(load_alias '{path: true}' \
    $'--- &saved {foo: 42}\n---\ncopy: *saved.foo\n')
  is "$output" '[{"foo":42},{"copy":42}]' \
    "$parser automatically enables stream scope for paths"

  set +e
  output=$(load_alias '{path: true, stream: false}' \
    $'--- &saved {foo: 42}\n---\ncopy: *saved.foo\n' 2>&1)
  status=$?
  set -e
  is "$status" 1 "$parser honors explicit document scope"
  like "$output" 'path "saved.foo" segment "saved": unknown anchor' \
    "$parser reports the unavailable base anchor"

  set +e
  output=$(load_alias '{path: true}' \
    $'base: &x {foo: 42}\ncopy: *x.missing\n' 2>&1)
  status=$?
  set -e
  is "$status" 1 "$parser rejects missing mapping keys"
  like "$output" 'path "x.missing" segment "missing": missing mapping key' \
    "$parser reports the full alias and failing mapping segment"

  set +e
  output=$(load_alias '{path: true}' \
    $'base: &x [one]\ncopy: *x.999999999999999999999999\n' 2>&1)
  status=$?
  set -e
  is "$status" 1 "$parser rejects overflowing sequence indices"
  like "$output" 'sequence index out of range' \
    "$parser reports the index error"
done

done-testing
