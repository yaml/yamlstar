## Elixir Usage

Use `yamlstar` as a drop-in replacement for your current YAML
loader:

File `main.exs`:

```elixir
{:ok, data} = YAMLStar.load(File.read!("config.yaml"))
IO.inspect(data)

# Or the raising variant:
data = YAMLStar.load!(File.read!("config.yaml"))
```


## Installation

Add `yamlstar` to your `mix.exs` deps and install the `libyamlstar.so`
shared library:

```elixir
def deps do
  [
    {:yamlstar, "~> 0.1.15"}
  ]
end
```

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

See <https://yamlstar.org/doc/install/> for more info.


### Requirements

* Elixir 1.18 or higher (Erlang/OTP 27+)
* A C compiler (the package builds a small NIF shim)
* Linux or macOS
