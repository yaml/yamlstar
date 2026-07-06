# Copyright 2023-2026 Ingy dot Net
# This code is licensed under MIT license (See License for details)

defmodule YAMLStarTest do
  use ExUnit.Case, async: true

  test "load mapping" do
    assert {:ok, %{"test" => 42}} =
             YAMLStar.load("test: 42")
  end

  test "load plain yaml" do
    assert {:ok, %{"foo" => "bar"}} = YAMLStar.load("foo: bar")
  end

  test "load error returns cause" do
    assert {:error, cause} = YAMLStar.load(":")
    assert is_binary(cause)
  end

  test "load! raises" do
    assert_raise YAMLStar.Error, fn ->
      YAMLStar.load!(":")
    end
  end

  test "load multiple times" do
    for _ <- 1..4 do
      assert {:ok, %{"test" => 42}} =
               YAMLStar.load("test: 42")
    end
  end
end
