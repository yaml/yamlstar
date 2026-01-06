using System.Runtime.InteropServices;

namespace YAMLStar.Native;

internal static class YAMLStarNative
{
    private const string LibraryName = "libyamlstar";

    [DllImport(LibraryName)]
    public static extern int graal_create_isolate(
        IntPtr params_ptr,
        IntPtr isolate_ptr,
        ref IntPtr isolate_thread_ptr);

    [DllImport(LibraryName)]
    public static extern int graal_tear_down_isolate(IntPtr isolate_thread_ptr);

    [DllImport(LibraryName)]
    public static extern IntPtr yamlstar_load(
        IntPtr isolate_thread_ptr,
        [MarshalAs(UnmanagedType.LPStr)] string yaml);

    [DllImport(LibraryName)]
    public static extern IntPtr yamlstar_load_all(
        IntPtr isolate_thread_ptr,
        [MarshalAs(UnmanagedType.LPStr)] string yaml);

    [DllImport(LibraryName)]
    public static extern IntPtr yamlstar_version(IntPtr isolate_thread_ptr);
}
