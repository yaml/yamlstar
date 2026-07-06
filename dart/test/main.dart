// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

// Test the yamlstar Dart binding.
// Run with: dart run test/main.dart

import 'dart:io';

import 'package:yamlstar/yamlstar.dart';

var fails = 0;

void check(bool cond, String label) {
  if (cond) {
    print('ok - $label');
  } else {
    print('not ok - $label');
    fails++;
  }
}

void main() {
  final yaml = YAMLStar();

  var data = yaml.load('test: 42');
  check(data['test'] == 42, 'load mapping');

  data = yaml.load('foo: bar');
  check(data['foo'] == 'bar', 'load plain yaml');

  var threw = false;
  try {
    yaml.load(':');
  } on YAMLStarError {
    threw = true;
  }
  check(threw, 'load error throws');
  check(yaml.error != null, 'error object is set');

  data = yaml.load('test: 42');
  check(data['test'] == 42, 'load multiple times');

  yaml.dispose();

  if (fails > 0) {
    stderr.writeln('$fails test(s) failed');
    exit(1);
  }
}
