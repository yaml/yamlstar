module TestYAMLStar

using Test
import YAMLStar as YS

@testset "YAMLStar" begin
    ys = YS.Runtime()

    @test YS.load(ys, "key: value")["key"] == "value"
    @test YS.load(ys, "- a\n- b\n") == ["a", "b"]
    @test YS.load_all(ys, "---\ndoc1\n---\na: 1\n")[2]["a"] == 1
    @test YS.dump(ys, Dict("key" => "value")) == "key: value\n"
    @test YS.dump_all(ys, ["doc1", Dict("a" => 1)]) == "---\ndoc1\n---\na: 1\n"
    @test occursin(r"^\d+\.\d+\.\d+(-SNAPSHOT)?$", YS.version(ys))

    YS.close(ys)
end

end
