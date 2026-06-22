require "./spec_helper"

describe YAMLStar do
  it "loads a mapping" do
    ys = YAMLStar.new
    ys.load("key: value")["key"].as_s.should eq("value")
    ys.close
  end

  it "loads a sequence" do
    ys = YAMLStar.new
    ys.load("- a\n- b\n").as_a.map(&.as_s).should eq(["a", "b"])
    ys.close
  end

  it "loads all documents" do
    ys = YAMLStar.new
    ys.load_all("---\ndoc1\n---\na: 1\n")[1]["a"].as_i.should eq(1)
    ys.close
  end

  it "dumps values" do
    ys = YAMLStar.new
    ys.dump({"key" => "value"}).should eq("key: value\n")
    ys.close
  end

  it "dumps all documents" do
    ys = YAMLStar.new
    ys.dump_all(["doc1", {"a" => 1}]).should eq("---\ndoc1\n---\na: 1\n")
    ys.close
  end

  it "reports the library version" do
    ys = YAMLStar.new
    ys.version.should match(/\A\d+\.\d+\.\d+(-SNAPSHOT)?\z/)
    ys.close
  end
end
