using System.Runtime.InteropServices;

namespace YAMLStar.Native;

internal static class YAMLStarNative
{
    private const string LibraryName = "libyamlstar";

    [DllImport(LibraryName)]
    public static extern IntPtr yamlstar_load(
        [MarshalAs(UnmanagedType.LPStr)] string yaml,
        [MarshalAs(UnmanagedType.LPStr)] string opts);

    [DllImport(LibraryName)]
    public static extern IntPtr yamlstar_load_all(
        [MarshalAs(UnmanagedType.LPStr)] string yaml,
        [MarshalAs(UnmanagedType.LPStr)] string opts);

    [DllImport(LibraryName)]
    public static extern IntPtr yamlstar_version();
}
