#!/usr/bin/env perl

use strict;
use warnings;

use Test2::V0;
use FindBin;
use lib "$FindBin::Bin/../lib";

use YAMLStar;
use JSON::PP ();

# Create a YAMLStar instance:
my $ys = YAMLStar->new();
isa_ok($ys, 'YAMLStar');

# Test simple scalar:
is($ys->load("hello"), "hello", 'Load simple scalar');

# Test integer:
is($ys->load("42"), 42, 'Load integer');

# Test float:
is($ys->load("3.14"), 3.14, 'Load float');

# Test boolean true:
is($ys->load("true"), JSON::PP::true, 'Load boolean true');

# Test boolean false:
is($ys->load("false"), JSON::PP::false, 'Load boolean false');

# Test null:
is($ys->load("null"), undef, 'Load null');

# Test simple mapping:
is($ys->load("key: value"), {key => "value"}, 'Load simple mapping');

# Test nested mapping:
is($ys->load("\
outer:
  inner: value
"),
  { outer => {inner => "value"},
  },
  'Load nested mapping');

# Test mapping with multiple keys:
is($ys->load("\
key1: value1
key2: value2
key3: value3"),
  { key1 => "value1",
    key2 => "value2",
    key3 => "value3",
  },
  'Load mapping with multiple keys');

# Test simple sequence:
is($ys->load("\
- item1
- item2
- item3
"),
  [ "item1",
    "item2",
    "item3",
  ],
  'Load simple sequence');

# Test flow sequence:
is($ys->load("[a, b, c]"),
   ["a", "b", "c"],
   'Load flow sequence');

# Test type coercion:
is($ys->load("\
string: hello
integer: 42
float: 3.14
bool_true: true
bool_false: false
null_value: null
"),
  { string => "hello",
    integer => 42,
    float => 3.14,
    bool_true => JSON::PP::true,
    bool_false => JSON::PP::false,
    null_value => undef,
  },
  'Load with type coercion');

# Test sequence of mappings:
is($ys->load("\
- name: Alice
  age: 30
- name: Bob
  age: 25
"),
  [ {name => "Alice", age => 30},
    {name => "Bob", age => 25},
  ],
  'Load sequence of mappings');

# Test mapping with sequence values:
is($ys->load("\
fruits:
  - apple
  - banana
colors:
  - red
  - blue
"),
  { fruits => ["apple", "banana"],
    colors => ["red", "blue"],
  },
  'Load mapping with sequence values');

# Test load_all with single document:
is($ys->load_all("hello"), ["hello"], 'Load_all with single document');

# Test load_all with multiple documents:
is($ys->load_all("\
---
doc1
---
doc2
---
doc3
"),
  [ "doc1",
    "doc2",
    "doc3",
  ],
  'Load_all with multiple documents');

# Test load_all with explicit markers:
is($ys->load_all("\
---
a: 1
...
---
b: 2
...
"),
  [ {a => 1},
    {b => 2},
  ],
  'Load_all with explicit markers');

# Test version:
like($ys->version(), qr/.+/, 'Version returns a string');

# Test error handling with malformed YAML:
like(dies { $ys->load('key: "unclosed') }, qr/libyamlstar:/, 'Dies with libyamlstar error on malformed YAML');

# Test empty document:
is($ys->load(""), undef, 'Load empty document');

# Test whitespace only:
is($ys->load("\


  
"),
  undef,
  'Load whitespace only');

# Test quoted strings:
is($ys->load("'hello world'"), "hello world", 'Load single-quoted string');
is($ys->load('"hello world"'), "hello world", 'Load double-quoted string');

done_testing;
