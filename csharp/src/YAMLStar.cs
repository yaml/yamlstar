using System.Text.Json;
using System.Runtime.InteropServices;
using YAMLStar.Native;

namespace YAMLStar;

public sealed class YAMLStar : IDisposable
{
    private readonly IntPtr _isolateThread;
    private bool _disposed;

    public YAMLStar()
    {
        _isolateThread = IntPtr.Zero;
        var rc = YAMLStarNative.graal_create_isolate(
            IntPtr.Zero,
            IntPtr.Zero,
            ref _isolateThread);

        if (rc != 0 || _isolateThread == IntPtr.Zero)
        {
            throw new YAMLStarException("Failed to create GraalVM isolate");
        }
    }

    public object? Load(string yaml)
    {
        if (_disposed)
        {
            throw new ObjectDisposedException(nameof(YAMLStar));
        }

        var result = YAMLStarNative.yamlstar_load(_isolateThread, yaml);

        if (result == IntPtr.Zero)
        {
            return null;
        }

        var json = Marshal.PtrToStringAnsi(result);
        if (json == null)
        {
            return null;
        }

        return ParseResponse(json);
    }

    public object? LoadAll(string yaml)
    {
        if (_disposed)
        {
            throw new ObjectDisposedException(nameof(YAMLStar));
        }

        var result = YAMLStarNative.yamlstar_load_all(_isolateThread, yaml);

        if (result == IntPtr.Zero)
        {
            return null;
        }

        var json = Marshal.PtrToStringAnsi(result);
        if (json == null)
        {
            return null;
        }

        return ParseResponse(json);
    }

    public string? Version()
    {
        if (_disposed)
        {
            throw new ObjectDisposedException(nameof(YAMLStar));
        }

        var result = YAMLStarNative.yamlstar_version(_isolateThread);

        if (result == IntPtr.Zero)
        {
            return null;
        }

        return Marshal.PtrToStringAnsi(result);
    }

    private object? ParseResponse(string json)
    {
        try
        {
            var response = JsonSerializer.Deserialize<JsonElement>(json);

            // Check for error in response
            if (response.TryGetProperty("error", out var errorElement))
            {
                var error = errorElement.GetProperty("cause").GetString();
                throw new YAMLStarException(error ?? "Unknown error");
            }

            // Get the data from response
            if (response.TryGetProperty("data", out var dataElement))
            {
                return JsonSerializer.Deserialize<object>(dataElement.GetRawText());
            }

            return null;
        }
        catch (JsonException ex)
        {
            throw new YAMLStarException($"Failed to parse response: {ex.Message}");
        }
    }

    public void Dispose()
    {
        if (!_disposed && _isolateThread != IntPtr.Zero)
        {
            var rc = YAMLStarNative.graal_tear_down_isolate(_isolateThread);
            if (rc != 0)
            {
                // Log error but don't throw since we're in Dispose
                Console.Error.WriteLine($"Failed to tear down isolate: {rc}");
            }
            _disposed = true;
        }
    }
}
