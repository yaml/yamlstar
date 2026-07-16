-module(yamlstar_test).
-export([run/0]).

run() ->
  {ok, #{<<"test">> := 42}} =
    yamlstar:load(<<"test: 42">>),
  io:format("ok - load yaml~n").
