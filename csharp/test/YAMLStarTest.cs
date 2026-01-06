using Xunit;
using YAMLStar;
using System.Text.Json;

namespace YAMLStar.Tests;

public class YAMLStarTest
{
    [Fact]
    public void TestRuntimeInitialization()
    {
        var ys = new YAMLStar();
        Assert.NotNull(ys);
        ys.Dispose();
    }

    [Fact]
    public void TestLoadSimpleScalar()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("hello");
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal("hello", element.GetString());
    }

    [Fact]
    public void TestLoadInteger()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("42");
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal(42, element.GetInt32());
    }

    [Fact]
    public void TestLoadFloat()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("3.14");
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal(3.14, element.GetDouble(), precision: 2);
    }

    [Fact]
    public void TestLoadBooleanTrue()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("true");
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.True(element.GetBoolean());
    }

    [Fact]
    public void TestLoadBooleanFalse()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("false");
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.False(element.GetBoolean());
    }

    [Fact]
    public void TestLoadNull()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("null");
        if (result != null)
        {
            var element = Assert.IsType<JsonElement>(result);
            Assert.Equal(JsonValueKind.Null, element.ValueKind);
        }
        else
        {
            Assert.Null(result);
        }
    }

    [Fact]
    public void TestLoadSimpleMapping()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("key: value");
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal(JsonValueKind.Object, element.ValueKind);
        Assert.Equal("value", element.GetProperty("key").GetString());
    }

    [Fact]
    public void TestLoadNestedMapping()
    {
        using var ys = new YAMLStar();
        var yaml = @"
outer:
  inner: value
";
        var result = ys.Load(yaml);
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal("value", element.GetProperty("outer").GetProperty("inner").GetString());
    }

    [Fact]
    public void TestLoadSimpleSequence()
    {
        using var ys = new YAMLStar();
        var yaml = @"
- item1
- item2
- item3
";
        var result = ys.Load(yaml);
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal(JsonValueKind.Array, element.ValueKind);
        Assert.Equal(3, element.GetArrayLength());
        Assert.Equal("item1", element[0].GetString());
        Assert.Equal("item2", element[1].GetString());
        Assert.Equal("item3", element[2].GetString());
    }

    [Fact]
    public void TestLoadFlowSequence()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("[a, b, c]");
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal(JsonValueKind.Array, element.ValueKind);
        Assert.Equal(3, element.GetArrayLength());
    }

    [Fact]
    public void TestLoadAll()
    {
        using var ys = new YAMLStar();
        var yaml = @"---
doc1
---
doc2
---
doc3";
        var result = ys.LoadAll(yaml);
        Assert.NotNull(result);
        var element = Assert.IsType<JsonElement>(result);
        Assert.Equal(JsonValueKind.Array, element.ValueKind);
        Assert.Equal(3, element.GetArrayLength());
        Assert.Equal("doc1", element[0].GetString());
        Assert.Equal("doc2", element[1].GetString());
        Assert.Equal("doc3", element[2].GetString());
    }

    [Fact]
    public void TestVersion()
    {
        using var ys = new YAMLStar();
        var version = ys.Version();
        Assert.NotNull(version);
        Assert.NotEmpty(version);
    }

    [Fact]
    public void TestErrorHandling()
    {
        using var ys = new YAMLStar();
        Assert.Throws<YAMLStarException>(() => ys.Load("key: \"unclosed"));
    }

    [Fact]
    public void TestDispose()
    {
        var ys = new YAMLStar();
        ys.Dispose();

        // Should throw when disposed
        Assert.Throws<ObjectDisposedException>(() => ys.Load("test: value"));
    }

    [Fact]
    public void TestEmptyDocument()
    {
        using var ys = new YAMLStar();
        var result = ys.Load("");
        if (result != null)
        {
            var element = Assert.IsType<JsonElement>(result);
            Assert.Equal(JsonValueKind.Null, element.ValueKind);
        }
        else
        {
            Assert.Null(result);
        }
    }

    [Fact]
    public void TestQuotedStrings()
    {
        using var ys = new YAMLStar();
        var result1 = ys.Load("'hello world'");
        Assert.NotNull(result1);
        var element1 = Assert.IsType<JsonElement>(result1);
        Assert.Equal("hello world", element1.GetString());

        var result2 = ys.Load("\"hello world\"");
        Assert.NotNull(result2);
        var element2 = Assert.IsType<JsonElement>(result2);
        Assert.Equal("hello world", element2.GetString());
    }
}
