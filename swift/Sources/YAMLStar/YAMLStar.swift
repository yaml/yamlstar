// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

// Swift binding/API for the libyamlstar shared library.
//
// This module is a Swift port of the Python 'yamlstar' module, which
// is the reference implementation for YAMLStar FFI bindings to libyamlstar.
//
// The current user facing API consists of a single class, `YAMLStar`,
// which has a single method: `.load(string)`.
// The load() method takes a YAML string as input and returns the
// object that YAMLStar loads.

#if os(Linux)
    import Glibc
#elseif os(macOS)
    import Darwin
#else
    #error("Unsupported platform for yamlstar.")
#endif
import Foundation

// This value is automatically updated by 'make bump'.
// The version number is used to find the correct shared library file.
// We currently only support binding to an exact version of libyamlstar.
public let yamlstarVersion = "0.1.11"

#if os(Linux)
    let libyamlstarName = "libyamlstar.so.\(yamlstarVersion)"
#elseif os(macOS)
    let libyamlstarName = "libyamlstar.dylib.\(yamlstarVersion)"
#endif

// FFI signatures for the 3 libyamlstar functions used by this binding:
private typealias CreateIsolateFn = @convention(c) (
    UnsafeMutableRawPointer?,
    UnsafeMutablePointer<UnsafeMutableRawPointer?>?,
    UnsafeMutablePointer<UnsafeMutableRawPointer?>?
) -> Int32
private typealias TearDownIsolateFn = @convention(c) (
    UnsafeMutableRawPointer?
) -> Int32
private typealias LoadYamlstarFn = @convention(c) (
    UnsafeMutableRawPointer?, UnsafePointer<CChar>?
) -> UnsafePointer<CChar>?

/// Error thrown by the YAMLStar loader.
public struct YAMLStarError: Error, CustomStringConvertible {
    public let message: String
    public var description: String { "YAMLStarError: \(message)" }
}

// Find the libyamlstar shared library file path.
// Search LD_LIBRARY_PATH entries, then common install locations:
private func findLibyamlstarPath() throws -> String {
    var paths: [String] = []
    let env = ProcessInfo.processInfo.environment
    if let libraryPath = env["LD_LIBRARY_PATH"] {
        paths.append(
            contentsOf: libraryPath.split(separator: ":").map(String.init))
    }
    paths.append("/usr/local/lib")
    if let home = env["HOME"] {
        paths.append("\(home)/.local/lib")
    }

    for dir in paths {
        let path = "\(dir)/\(libyamlstarName)"
        if FileManager.default.fileExists(atPath: path) {
            return path
        }
    }

    throw YAMLStarError(
        message: """
            Shared library file '\(libyamlstarName)' not found
            Try: curl https://yamlstar.org/install | \
            VERSION=\(yamlstarVersion) LIB=1 bash
            See: https://github.com/yaml/yamlstar/wiki/\
            Installing-YAMLStar
            """)
}

/// The YAMLStar class is the main user facing API for this module.
///
/// Usage:
///     import YAMLStar
///     let yaml = try YAMLStar()
///     let data = try yaml.load(input)
public final class YAMLStar {
    private let handle: UnsafeMutableRawPointer
    private var isolateThread: UnsafeMutableRawPointer?
    private let loadYamlstar: LoadYamlstarFn
    private let tearDownIsolate: TearDownIsolateFn

    /// The error object from the last load() call, if any.
    public private(set) var error: [String: Any]?

    // Load libyamlstar and create a GraalVM isolate for the life of the
    // YAMLStar instance:
    public init() throws {
        let path = try findLibyamlstarPath()
        guard let handle = dlopen(path, RTLD_NOW) else {
            throw YAMLStarError(
                message: "Failed to load shared library '\(path)'")
        }
        self.handle = handle

        func symbol<T>(_ name: String, _ type: T.Type) throws -> T {
            guard let sym = dlsym(handle, name) else {
                throw YAMLStarError(
                    message: "Symbol '\(name)' not found in libyamlstar")
            }
            return unsafeBitCast(sym, to: T.self)
        }

        let createIsolate = try symbol(
            "graal_create_isolate", CreateIsolateFn.self)
        tearDownIsolate = try symbol(
            "graal_tear_down_isolate", TearDownIsolateFn.self)
        loadYamlstar = try symbol(
            "yamlstar_load", LoadYamlstarFn.self)

        // Create a new GraalVM isolatethread for the instance:
        guard createIsolate(nil, nil, &isolateThread) == 0 else {
            throw YAMLStarError(message: "Failed to create isolate")
        }
    }

    /// Load a YAML string and return the result.
    public func load(_ input: String) throws -> Any? {
        // Reset any previous error:
        error = nil

        // Call 'yamlstar_load' function in libyamlstar shared library:
        guard let respPtr = loadYamlstar(isolateThread, input) else {
            throw YAMLStarError(message: "Null response from 'libyamlstar'")
        }

        // Decode the JSON response:
        let respData = Data(String(cString: respPtr).utf8)
        let resp = try JSONSerialization.jsonObject(
            with: respData, options: [.fragmentsAllowed]
        )
        guard let resp = resp as? [String: Any] else {
            throw YAMLStarError(
                message: "Unexpected response from 'libyamlstar'")
        }

        // Check for libyamlstar error in JSON response:
        if let err = resp["error"] as? [String: Any] {
            error = err
            throw YAMLStarError(
                message: err["cause"] as? String ?? "unknown error")
        }

        // Get the response object from loading the YAML string:
        guard resp.keys.contains("data") else {
            throw YAMLStarError(
                message: "Unexpected response from 'libyamlstar'")
        }

        // Return the response object ('data' may be JSON null):
        return resp["data"]
    }

    deinit {
        // Tear down the isolate thread to free resources:
        if tearDownIsolate(isolateThread) != 0 {
            FileHandle.standardError.write(
                Data("Failed to tear down isolate\n".utf8))
        }
        dlclose(handle)
    }
}
