defmodule YAMLStar do
  # Copyright 2023-2026 Ingy dot Net
  # This code is licensed under MIT license (See License for details)

  @moduledoc """
  Elixir binding/API for the libyamlstar shared library.

  This module is an Elixir port of the Python 'yamlstar' module,
  the reference implementation for YAMLStar FFI bindings to libyamlstar.

  The `load/1` function takes a YAML string as input and returns
  `{:ok, data}` with the loaded value, or `{:error, message}`. The
  `load!/1` variant returns the data directly and raises
  `YAMLStar.Error` on failure.
  """

  @on_load :load_nif

  @doc false
  def load_nif do
    path = :filename.join(:code.priv_dir(:yamlstar), ~c"yamlstar_nif")
    :erlang.load_nif(path, 0)
  end

  @doc """
  Load a YAML string and return the result.
  """
  @spec load(String.t()) :: {:ok, term()} | {:error, String.t()}
  def load(input) when is_binary(input) do
    case nif_yamlstar_load(input) do
      {:error, message} ->
        {:error, message}

      json when is_binary(json) ->
        # Decode the JSON response and check for a libyamlstar error:
        resp = JSON.decode!(json)

        cond do
          err = resp["error"] ->
            {:error, err["cause"]}

          Map.has_key?(resp, "data") ->
            {:ok, resp["data"]}

          true ->
            {:error, "Unexpected response from 'libyamlstar'"}
        end
    end
  end

  @doc """
  Like `load/1` but returns the data directly and raises
  `YAMLStar.Error` on failure.
  """
  @spec load!(String.t()) :: term()
  def load!(input) do
    case load(input) do
      {:ok, data} -> data
      {:error, message} -> raise YAMLStar.Error, message: message
    end
  end

  defp nif_yamlstar_load(_input) do
    :erlang.nif_error(:nif_not_loaded)
  end
end

defmodule YAMLStar.Error do
  @moduledoc "Error raised by `YAMLStar.load!/1`."
  defexception [:message]
end
