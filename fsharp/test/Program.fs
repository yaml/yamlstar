open System
open YAMLStar

[<EntryPoint>]
let main _ =
  use ys = new YAMLStar()
  let data = ys.Load("test: 42")

  if data.GetProperty("test").GetInt32() <> 42 then
    failwith "load yaml failed"

  printfn "ok - load yaml"
  0
