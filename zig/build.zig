const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{});

    // libc is required so that std.DynLib uses the system dlopen,
    // which is needed to load the GraalVM native-image libyamlstar library:
    const mod = b.addModule("yamlstar", .{
        .root_source_file = b.path("src/yamlstar.zig"),
        .target = target,
        .optimize = optimize,
        .link_libc = true,
    });

    const tests = b.addTest(.{ .root_module = mod });
    const run_tests = b.addRunArtifact(tests);
    const test_step = b.step("test", "Run unit tests");
    test_step.dependOn(&run_tests.step);
}
