// Copyright 2023-2026 Ingy dot Net
// This code is licensed under MIT license (See License for details)

//! Zig binding/API for the libyamlstar shared library.
//!
//! This module is a Zig port of the Python 'yamlstar' module, which is
//! the reference implementation for YAMLStar FFI bindings to libyamlstar.
//!
//! The current user facing API consists of a single struct, `YAMLStar`,
//! which has a single method: `.load(string)`.
//! The load() method takes a YAML string as input and returns the
//! JSON value that YAMLStar loads.

const std = @import("std");
const builtin = @import("builtin");

// This value is automatically updated by 'make bump'.
// The version number is used to find the correct shared library file.
// We currently only support binding to an exact version of libyamlstar.
pub const yamlstar_version = "0.1.11";

// We currently only support platforms that GraalVM supports.
// Windows uses an unversioned file name, matching the Python binding:
const libyamlstar_name = switch (builtin.os.tag) {
    .linux => "libyamlstar.so." ++ yamlstar_version,
    .macos => "libyamlstar.dylib." ++ yamlstar_version,
    .windows => "libyamlstar.dll",
    else => @compileError("Unsupported platform for yamlstar."),
};

const is_windows = builtin.os.tag == .windows;

// Windows finds DLLs via PATH; other platforms use LD_LIBRARY_PATH:
const lib_path_env = if (is_windows) "PATH" else "LD_LIBRARY_PATH";

// FFI signatures for the 3 libyamlstar functions used by this binding:
const CreateIsolateFn = *const fn (
    ?*anyopaque,
    ?*?*anyopaque,
    ?*?*anyopaque,
) callconv(.c) c_int;
const TearDownIsolateFn = *const fn (?*anyopaque) callconv(.c) c_int;
const LoadYamlstarFn = *const fn (
    ?*anyopaque,
    [*:0]const u8,
) callconv(.c) ?[*:0]const u8;

pub const Error = error{
    LibyamlstarNotFound,
    SymbolNotFound,
    IsolateCreateFailed,
    NullResponse,
    BadResponse,
    YAMLStarError,
};

/// The result of a successful YAMLStar.load() call.
/// Owns the JSON arena; call deinit() when done with the data.
pub const Result = struct {
    parsed: std.json.Parsed(std.json.Value),
    data: std.json.Value,

    pub fn deinit(self: Result) void {
        self.parsed.deinit();
    }
};

// Join a candidate path and return it (owned) if the file exists:
fn checkDir(allocator: std.mem.Allocator, dir: []const u8) ?[]u8 {
    const path = std.fs.path.join(
        allocator,
        &.{ dir, libyamlstar_name },
    ) catch return null;
    std.fs.cwd().access(path, .{}) catch {
        allocator.free(path);
        return null;
    };
    return path;
}

// Find the libyamlstar shared library file path (owned by caller).
// Search the platform library path entries, then common install
// locations:
fn findLibyamlstar(allocator: std.mem.Allocator) ?[]u8 {
    if (std.process.getEnvVarOwned(allocator, lib_path_env)) |paths| {
        defer allocator.free(paths);
        var dirs = std.mem.splitScalar(
            u8,
            paths,
            std.fs.path.delimiter,
        );
        while (dirs.next()) |dir| {
            if (dir.len == 0) continue;
            if (checkDir(allocator, dir)) |path| return path;
        }
    } else |_| {}

    if (!is_windows) {
        if (checkDir(allocator, "/usr/local/lib")) |path| return path;
    }

    for ([_][]const u8{ "HOME", "USERPROFILE" }) |env_name| {
        const home = std.process.getEnvVarOwned(
            allocator,
            env_name,
        ) catch continue;
        defer allocator.free(home);
        const dir = std.fs.path.join(
            allocator,
            &.{ home, ".local", "lib" },
        ) catch continue;
        defer allocator.free(dir);
        if (checkDir(allocator, dir)) |path| return path;
    }

    return null;
}

// Open the libyamlstar shared library or explain how to install it:
fn openLibyamlstar(allocator: std.mem.Allocator) !std.DynLib {
    const path = findLibyamlstar(allocator) orelse {
        std.log.err(
            \\Shared library file '{s}' not found
            \\Try: curl https://yamlstar.org/install | VERSION={s} LIB=1 bash
            \\See: https://github.com/yaml/yamlstar/wiki/Installing-YAMLStar
        , .{ libyamlstar_name, yamlstar_version });
        return Error.LibyamlstarNotFound;
    };
    defer allocator.free(path);
    return std.DynLib.open(path);
}

/// The YAMLStar struct is the main user facing API for this module.
///
/// Usage:
///   var yaml = try yamlstar.YAMLStar.init(allocator);
///   defer yaml.deinit();
///   var result = try yaml.load(input);
///   defer result.deinit();
///
/// A GraalVM isolate is thread-affine, so an instance must be used from
/// the thread that created it.
pub const YAMLStar = struct {
    allocator: std.mem.Allocator,
    lib: std.DynLib,
    isolate_thread: ?*anyopaque = null,
    yamlstar_load: LoadYamlstarFn,
    tear_down_isolate: TearDownIsolateFn,
    // The 'cause' message of the last YAMLStarError (owned):
    error_cause: ?[]u8 = null,

    /// Load libyamlstar and create a GraalVM isolate for the life of the
    /// YAMLStar instance.
    pub fn init(allocator: std.mem.Allocator) !YAMLStar {
        var lib = try openLibyamlstar(allocator);
        errdefer lib.close();

        const create_isolate = lib.lookup(
            CreateIsolateFn,
            "graal_create_isolate",
        ) orelse return Error.SymbolNotFound;

        var self = YAMLStar{
            .allocator = allocator,
            .lib = lib,
            .yamlstar_load = lib.lookup(
                LoadYamlstarFn,
                "yamlstar_load",
            ) orelse return Error.SymbolNotFound,
            .tear_down_isolate = lib.lookup(
                TearDownIsolateFn,
                "graal_tear_down_isolate",
            ) orelse return Error.SymbolNotFound,
        };

        if (create_isolate(null, null, &self.isolate_thread) != 0)
            return Error.IsolateCreateFailed;

        return self;
    }

    /// Load a YAML string and return the Result.
    /// On Error.YAMLStarError the message is in self.error_cause.
    pub fn load(self: *YAMLStar, input: []const u8) !Result {
        // Reset any previous error:
        if (self.error_cause) |cause| {
            self.allocator.free(cause);
            self.error_cause = null;
        }

        const input_z = try self.allocator.dupeZ(u8, input);
        defer self.allocator.free(input_z);

        // Call 'yamlstar_load' function in libyamlstar shared library.
        // The returned C string is owned by the GraalVM heap:
        const resp_ptr = self.yamlstar_load(
            self.isolate_thread,
            input_z,
        ) orelse return Error.NullResponse;

        // Decode the JSON response:
        var parsed = try std.json.parseFromSlice(
            std.json.Value,
            self.allocator,
            std.mem.span(resp_ptr),
            .{},
        );
        errdefer parsed.deinit();

        const resp = switch (parsed.value) {
            .object => |object| object,
            else => return Error.BadResponse,
        };

        // Check for libyamlstar error in JSON response:
        if (resp.get("error")) |err| {
            if (err == .object) {
                if (err.object.get("cause")) |cause| {
                    if (cause == .string) {
                        self.error_cause =
                            try self.allocator.dupe(u8, cause.string);
                    }
                }
            }
            return Error.YAMLStarError;
        }

        // Get the data value from loading the YAML string:
        const data = resp.get("data") orelse return Error.BadResponse;

        return Result{ .parsed = parsed, .data = data };
    }

    /// Tear down the GraalVM isolate and close libyamlstar:
    pub fn deinit(self: *YAMLStar) void {
        if (self.tear_down_isolate(self.isolate_thread) != 0)
            std.log.warn("Failed to tear down isolate", .{});
        self.lib.close();
        if (self.error_cause) |cause| self.allocator.free(cause);
        self.* = undefined;
    }
};

test "load mapping" {
    var yaml = try YAMLStar.init(std.testing.allocator);
    defer yaml.deinit();

    var result = try yaml.load("test: 42");
    defer result.deinit();

    try std.testing.expectEqual(
        @as(i64, 42),
        result.data.object.get("test").?.integer,
    );
}

test "load plain yaml" {
    var yaml = try YAMLStar.init(std.testing.allocator);
    defer yaml.deinit();

    var result = try yaml.load("foo: bar");
    defer result.deinit();

    try std.testing.expectEqualStrings(
        "bar",
        result.data.object.get("foo").?.string,
    );
}

test "load error" {
    var yaml = try YAMLStar.init(std.testing.allocator);
    defer yaml.deinit();

    try std.testing.expectError(
        Error.YAMLStarError,
        yaml.load("key: \"unclosed"),
    );
    try std.testing.expect(yaml.error_cause != null);
}

test "load multiple times" {
    var yaml = try YAMLStar.init(std.testing.allocator);
    defer yaml.deinit();

    for (0..2) |_| {
        var result = try yaml.load("test: 42");
        defer result.deinit();

        try std.testing.expectEqual(
            @as(i64, 42),
            result.data.object.get("test").?.integer,
        );
    }
}
