// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

/// Dart binding/API for the libyamlstar shared library.
///
/// This module is a Dart port of the Python 'yamlstar' module, which is
/// the reference implementation for YAMLStar FFI bindings to libyamlstar.
///
/// The current user facing API consists of a single class, `YAMLStar`,
/// which has a single method: `.load(string)`.
/// The load() method takes a YAML string as input and returns the
/// Dart object that YAMLStar loads.
library;

import 'dart:convert';
import 'dart:ffi';
import 'dart:io';

import 'package:ffi/ffi.dart';

// This value is automatically updated by 'make bump'.
// The version number is used to find the correct shared library file.
// We currently only support binding to an exact version of libyamlstar.
const String yamlstarVersion = '0.1.17';

typedef _CreateIsolateC = Int32 Function(
  Pointer<Void>,
  Pointer<Pointer<Void>>,
  Pointer<Pointer<Void>>,
);
typedef _CreateIsolateDart = int Function(
  Pointer<Void>,
  Pointer<Pointer<Void>>,
  Pointer<Pointer<Void>>,
);
typedef _TearDownIsolateC = Int32 Function(Pointer<Void>);
typedef _TearDownIsolateDart = int Function(Pointer<Void>);
typedef _LoadYamlstarC = Pointer<Utf8> Function(Pointer<Void>, Pointer<Utf8>);
typedef _LoadYamlstarDart = Pointer<Utf8> Function(
  Pointer<Void>,
  Pointer<Utf8>,
);

/// Error thrown by the YAMLStar loader.
class YAMLStarError implements Exception {
  final String message;
  YAMLStarError(this.message);
  @override
  String toString() => 'YAMLStarError: $message';
}

// Find the libyamlstar shared library file path:
String _findLibyamlstarPath() {
  // We currently only support platforms that GraalVM supports.
  // Windows uses an unversioned file name, matching the Python binding:
  final String libyamlstarName;
  if (Platform.isLinux) {
    libyamlstarName = 'libyamlstar.so.$yamlstarVersion';
  } else if (Platform.isMacOS) {
    libyamlstarName = 'libyamlstar.dylib.$yamlstarVersion';
  } else if (Platform.isWindows) {
    libyamlstarName = 'libyamlstar.dll';
  } else {
    throw YAMLStarError(
      "Unsupported platform '${Platform.operatingSystem}' for yamlstar.",
    );
  }

  // Use the platform library path plus common install locations:
  final envName = Platform.isWindows ? 'PATH' : 'LD_LIBRARY_PATH';
  final sep = Platform.isWindows ? ';' : ':';
  final paths = <String>[];
  final libraryPath = Platform.environment[envName];
  if (libraryPath != null) {
    paths.addAll(libraryPath.split(sep).where((p) => p.isNotEmpty));
  }
  if (!Platform.isWindows) {
    paths.add('/usr/local/lib');
  }
  final home =
      Platform.environment['HOME'] ?? Platform.environment['USERPROFILE'];
  if (home != null) {
    paths.add(
      [home, '.local', 'lib'].join(Platform.pathSeparator),
    );
  }

  for (final dir in paths) {
    final path = '$dir${Platform.pathSeparator}$libyamlstarName';
    if (File(path).existsSync()) return path;
  }

  throw YAMLStarError('''
Shared library file '$libyamlstarName' not found
Try: curl https://yamlstar.org/install | VERSION=$yamlstarVersion LIB=1 bash
See: https://github.com/yaml/yamlstar/wiki/Installing-YAMLStar
''');
}

/// The YAMLStar class is the main user facing API for this module.
///
/// Usage:
///   import 'package:yamlstar/yamlstar.dart';
///   final yaml = YAMLStar();
///   final data = yaml.load(File('file.ys').readAsStringSync());
class YAMLStar {
  late final DynamicLibrary _libyamlstar;
  late final _LoadYamlstarDart _loadYamlstar;
  late final _TearDownIsolateDart _tearDownIsolate;
  late final Pointer<Void> _isolateThread;

  /// The error object from the last load() call, if any.
  Map<String, dynamic>? error;

  /// Load libyamlstar and create a GraalVM isolate for the life of the
  /// YAMLStar instance.
  YAMLStar() {
    _libyamlstar = DynamicLibrary.open(_findLibyamlstarPath());

    final createIsolate =
        _libyamlstar.lookupFunction<_CreateIsolateC, _CreateIsolateDart>(
            'graal_create_isolate');
    _tearDownIsolate =
        _libyamlstar.lookupFunction<_TearDownIsolateC, _TearDownIsolateDart>(
            'graal_tear_down_isolate');
    _loadYamlstar = _libyamlstar
        .lookupFunction<_LoadYamlstarC, _LoadYamlstarDart>('yamlstar_load');

    // Create a new GraalVM isolatethread for the instance:
    final threadPtr = calloc<Pointer<Void>>();
    try {
      final rc = createIsolate(nullptr, nullptr, threadPtr);
      if (rc != 0) {
        throw YAMLStarError('Failed to create isolate');
      }
      _isolateThread = threadPtr.value;
    } finally {
      calloc.free(threadPtr);
    }
  }

  /// Load a YAML string and return the result.
  dynamic load(String input) {
    // Reset any previous error:
    error = null;

    // Call 'yamlstar_load' function in libyamlstar shared library:
    final inputPtr = input.toNativeUtf8();
    final String dataJson;
    try {
      final respPtr = _loadYamlstar(_isolateThread, inputPtr);
      dataJson = respPtr.toDartString();
    } finally {
      calloc.free(inputPtr);
    }

    // Decode the JSON response:
    final resp = jsonDecode(dataJson) as Map<String, dynamic>;

    // Check for libyamlstar error in JSON response:
    final err = resp['error'];
    if (err != null) {
      error = err as Map<String, dynamic>;
      throw YAMLStarError(error!['cause'] as String);
    }

    // Get the response object from loading the YAML string:
    if (!resp.containsKey('data')) {
      throw YAMLStarError("Unexpected response from 'libyamlstar'");
    }

    // Return the response object:
    return resp['data'];
  }

  /// Tear down the GraalVM isolate to free resources:
  void dispose() {
    final rc = _tearDownIsolate(_isolateThread);
    if (rc != 0) {
      throw YAMLStarError('Failed to tear down isolate');
    }
  }
}
