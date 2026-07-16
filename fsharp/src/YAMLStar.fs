namespace YAMLStar

open System
open System.Runtime.InteropServices
open System.Text.Json

exception YAMLStarException of string

module Native =
  [<Literal>]
  let LibraryName = "libyamlstar"

  [<DllImport(LibraryName)>]
  extern int graal_create_isolate(
    nativeint paramsPtr,
    nativeint isolatePtr,
    nativeint& threadPtr)

  [<DllImport(LibraryName)>]
  extern int graal_tear_down_isolate(nativeint threadPtr)

  [<DllImport(LibraryName)>]
  extern nativeint yamlstar_load(nativeint threadPtr, string yaml)

type YAMLStar() =
  let mutable thread = nativeint 0
  let rc = Native.graal_create_isolate(nativeint 0, nativeint 0, &thread)

  do
    if rc <> 0 || thread = nativeint 0 then
      raise (YAMLStarException "Failed to create isolate")

  member _.LoadJson(input: string) =
    let ptr = Native.yamlstar_load(thread, input)
    if ptr = nativeint 0 then
      raise (YAMLStarException "Null response from libyamlstar")
    Marshal.PtrToStringAnsi(ptr)

  member this.Load(input: string) =
    use doc = JsonDocument.Parse(this.LoadJson(input))
    let root = doc.RootElement
    if root.TryGetProperty("error") |> fst then
      let err = root.GetProperty("error")
      raise (YAMLStarException(err.GetProperty("cause").GetString()))
    root.GetProperty("data").Clone()

  interface IDisposable with
    member _.Dispose() =
      if thread <> nativeint 0 then
        let rc = Native.graal_tear_down_isolate(thread)
        thread <- nativeint 0
        if rc <> 0 then
          raise (YAMLStarException "Failed to tear down isolate")
