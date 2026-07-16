$Source = @'
using System;
using System.Runtime.InteropServices;

public static class YAMLStarNative {
  [DllImport("libyamlstar")]
  public static extern int graal_create_isolate(
    IntPtr parameters,
    IntPtr isolate,
    ref IntPtr thread);

  [DllImport("libyamlstar")]
  public static extern int graal_tear_down_isolate(IntPtr thread);

  [DllImport("libyamlstar")]
  public static extern IntPtr yamlstar_load(IntPtr thread, string input);
}
'@

if (-not ('YAMLStarNative' -as [type])) {
  Add-Type -TypeDefinition $Source
}

function Invoke-YAMLStarJson {
  param(
    [Parameter(Mandatory, ValueFromPipeline)]
    [string] $InputObject
  )

  process {
    $thread = [IntPtr]::Zero
    $rc = [YAMLStarNative]::graal_create_isolate(
      [IntPtr]::Zero,
      [IntPtr]::Zero,
      [ref] $thread)
    if ($rc -ne 0 -or $thread -eq [IntPtr]::Zero) {
      throw 'Failed to create isolate'
    }

    $ptr = [YAMLStarNative]::yamlstar_load($thread, $InputObject)
    if ($ptr -eq [IntPtr]::Zero) {
      [void] [YAMLStarNative]::graal_tear_down_isolate($thread)
      throw 'Null response from libyamlstar'
    }

    $json = [Runtime.InteropServices.Marshal]::PtrToStringAnsi($ptr)
    $rc = [YAMLStarNative]::graal_tear_down_isolate($thread)
    if ($rc -ne 0) {
      throw 'Failed to tear down isolate'
    }
    $json
  }
}

function Invoke-YAMLStar {
  param(
    [Parameter(Mandatory, ValueFromPipeline)]
    [string] $InputObject
  )

  process {
    $response = Invoke-YAMLStarJson $InputObject | ConvertFrom-Json
    if ($null -ne $response.error) {
      throw $response.error.cause
    }
    $response.data
  }
}

Export-ModuleMember -Function Invoke-YAMLStar, Invoke-YAMLStarJson
