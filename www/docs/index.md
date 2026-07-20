---
hide:
  - navigation
  - toc
---

<div class="home-hero">
  <h1 class="home-title">
    <span class="star-icon">★</span> YAMLStar
  </h1>
  <p class="home-subtitle">A YAML load/dump framework for all programming languages</p>
</div>

<pre class="home-page">
<span class="ykey">YAMLStar</span><span class="ysep">:</span>
  <span class="ykey">Version</span><span class="ysep">:</span> <span class="yver">0.1.16</span>
  <span class="ykey">Description</span><span class="ysep">:</span>
    <span class="ystr">A pure YAML 1.2 loading and dumping</span>
    <span class="ystr">with cross-language consistency</span>
  <span class="ykey">Platforms</span><span class="ysep">:</span>
    <span class="ystr">Supports Linux, macOS and Windows</span>
    <span class="ystr">for almost all language bindings</span>
  <span class="ykey">32 Languages (and counting!)</span><span class="ysep">:</span>
  - <a href="https://alire.ada.dev/crates/yamlstar"><span class="ystr">Ada</span></a>
  - <a href="https://clojars.org/org.yamlstar/yamlstar"><span class="ystr">Clojure</span></a>
  - <a href="https://shardbox.org/shards/yamlstar"><span class="ystr">Crystal</span></a>
  - <a href="https://www.nuget.org/packages/YAMLStar/"><span class="ystr">C#</span></a>
  - <a href="https://code.dlang.org/packages/yamlstar"><span class="ystr">D</span></a>
  - <a href="https://pub.dev/packages/yamlstar"><span class="ystr">Dart</span></a>
  - <a href="https://github.com/yaml/yamlstar-delphi"><span class="ystr">Delphi</span></a>
  - <a href="https://tatin.dev/v1/packages/versions/yaml-yamlstar-0"><span class="ystr">Dyalog APL</span></a>
  - <a href="https://hex.pm/packages/yamlstar"><span class="ystr">Elixir</span></a>
  - <a href="https://hex.pm/packages/yamlstar_erlang"><span class="ystr">Erlang</span></a>
  - <a href="https://www.nuget.org/packages/YAMLStar.FSharp/"><span class="ystr">F#</span></a>
  - <a href="https://github.com/yaml/yamlstar-fortran"><span class="ystr">Fortran</span></a>
  - <a href="https://github.com/yaml/yamlstar-go"><span class="ystr">Go</span></a>
  - <a href="https://hackage.haskell.org/package/yamlstar"><span class="ystr">Haskell</span></a>
  - <a href="https://central.sonatype.com/artifact/com.yaml/yamlstar"><span class="ystr">Java</span></a>
  - <a href="https://juliahub.com/ui/Packages/General/YAMLStar"><span class="ystr">Julia</span></a>
  - <a href="https://clojars.org/org.yamlstar/kotlin-yamlstar"><span class="ystr">Kotlin</span></a>
  - <a href="https://luarocks.org/modules/ingy/yamlstar"><span class="ystr">Lua</span></a>
  - <a href="https://mooncakes.io/docs/ingydotnet/yamlstar"><span class="ystr">MoonBit</span></a>
  - <a href="https://nimble.directory/pkg/yamlstar"><span class="ystr">Nim</span></a>
  - <a href="https://www.npmjs.com/package/yamlstar"><span class="ystr">Node.js</span></a>
  - <a href="https://metacpan.org/pod/YAMLStar"><span class="ystr">Perl</span></a>
  - <a href="https://packagist.org/packages/yaml/yamlstar-php"><span class="ystr">PHP</span></a>
  - <a href="https://www.powershellgallery.com/packages/YAMLStar"><span class="ystr">PowerShell</span></a>
  - <a href="https://pypi.org/project/yamlstar/"><span class="ystr">Python</span></a>
  - <a href="https://cran.r-project.org/package=yamlstar"><span class="ystr">R</span></a>
  - <a href="https://raku.land/zef:ingy/YAMLStar"><span class="ystr">Raku</span></a>
  - <a href="https://rubygems.org/gems/yamlstar"><span class="ystr">Ruby</span></a>
  - <a href="https://crates.io/crates/yamlstar"><span class="ystr">Rust</span></a>
  - <a href="https://central.sonatype.com/artifact/org.yamlstar/scala-yamlstar"><span class="ystr">Scala</span></a>
  - <a href="https://github.com/yaml/yamlstar/tree/main/swift"><span class="ystr">Swift</span></a>
  - <a href="https://github.com/yaml/yamlstar/tree/main/zig"><span class="ystr">Zig</span></a>
  <span class="ykey">Features</span><span class="ysep">:</span>
    <span class="ykey">Spec Compliant</span><span class="ysep">:</span> <span class="ybool">true</span>   <span class="ycom"># 100% YAML 1.2 specification</span>
    <span class="ykey">Load Stack</span><span class="ysep">:</span> <span class="ybool">true</span>       <span class="ycom"># YAML to native values</span>
    <span class="ykey">Dump Stack</span><span class="ysep">:</span> <span class="ybool">true</span>       <span class="ycom"># Native values back to YAML</span>
    <span class="ykey">Lightweight</span><span class="ysep">:</span> <span class="ybool">true</span>      <span class="ycom"># Minimal dependencies</span>
    <span class="ykey">Extensible</span><span class="ysep">:</span> <span class="ybool">true</span>       <span class="ycom"># Plugin system (Phase 3)</span>

<span class="ykey">Quick Start</span><span class="ysep">:</span>
  <span class="ykey">Python</span><span class="ysep">:</span> |
    <span class="ycom"># pip install yamlstar</span>
    <span class="ykw">from</span> yamlstar <span class="ykw">import</span> YAMLStar

    ys = YAMLStar()
    data = ys.load(<span class="ystr">'key: value'</span>)
    <span class="ycom"># {'key': 'value'}</span>
    text = ys.dump({<span class="ystr">'foo'</span>: [[<span class="ystr">'bar'</span>]]})
    <span class="ycom"># foo:
    # - - bar</span>

  <span class="ykey">Node.js</span><span class="ysep">:</span> |
    <span class="ycom">// npm install yamlstar</span>
    <span class="ykw">const</span> YAMLStar = require(<span class="ystr">'yamlstar'</span>);

    <span class="ykw">const</span> ys = <span class="ykw">new</span> YAMLStar();
    <span class="ykw">const</span> data = ys.load(<span class="ystr">'key: value'</span>);
    <span class="ycom">// { key: 'value' }</span>
    <span class="ykw">const</span> text = ys.dump({foo: [[<span class="ystr">'bar'</span>]]});
    <span class="ycom">// foo:
    // - - bar</span>

  <span class="ykey">Clojure</span><span class="ysep">:</span> |
    <span class="ycom">; lein/deps.edn: org.yamlstar/yamlstar "0.1.16"</span>
    (<span class="ykw">require</span> '[yamlstar.core <span class="ykw">:as</span> yaml])

    (yaml/load <span class="ystr">"key: value"</span>)
    <span class="ycom">;=> {"key" "value"}</span>
    (yaml/dump {<span class="ystr">"foo"</span> [[<span class="ystr">"bar"</span>]]})
    <span class="ycom">;=> "foo:\n- - bar\n"</span>

  <span class="ykey">Go</span><span class="ysep">:</span> |
    <span class="ycom">// go get github.com/yaml/yamlstar-go</span>
    <span class="ykw">import</span> <span class="ystr">"github.com/yaml/yamlstar-go"</span>

    ys := yamlstar.New()
    data := ys.Load(<span class="ystr">"key: value"</span>)
    <span class="ycom">// map[string]interface{}{"key": "value"}</span>
    text := yamlstar.Dump(map[string]interface{}{<span class="ystr">"foo"</span>: [][]string{{<span class="ystr">"bar"</span>}}})
    <span class="ycom">// foo:
    // - - bar</span>

<span class="ykey">Architecture</span><span class="ysep">:</span>
  <span class="ykey">Native</span><span class="ysep">:</span>
    <span class="ystr">Written in Clojure and compiled to native binary</span>
    <span class="ystr">with <a href="https://www.graalvm.org/latest/reference-manual/native-image/">GraalVM native-image</a></span>
  <span class="ykey">Load Stack</span><span class="ysep">:</span>
  - <span class="ykey">Parser</span><span class="ysep">:</span> <span class="ystr">Pure Clojure YAML 1.2 parser</span>
  - <span class="ykey">Composer</span><span class="ysep">:</span> <span class="ystr">Event stream to node tree</span>
  - <span class="ykey">Resolver</span><span class="ysep">:</span> <span class="ystr">Type inference (!!str, !!int, !!bool, etc.)</span>
  - <span class="ykey">Constructor</span><span class="ysep">:</span> <span class="ystr">Nodes to native data structures</span>
  <span class="ykey">Dump Stack</span><span class="ysep">:</span>
  - <span class="ykey">Representer</span><span class="ysep">:</span> <span class="ystr">Native data to node tree</span>
  - <span class="ykey">Desolver</span><span class="ysep">:</span> <span class="ystr">Minimal tags and scalar styles</span>
  - <span class="ykey">Serializer</span><span class="ysep">:</span> <span class="ystr">Node tree to event stream</span>
  - <span class="ykey">Emitter</span><span class="ysep">:</span> <span class="ystr">Events to YAML text</span>
  <span class="ykey">Dependencies</span><span class="ysep">:</span> <span class="ynum">0</span>  <span class="ycom"># Zero external dependencies</span>

<span class="ykey">Resources</span><span class="ysep">:</span>
  <span class="ykey">GitHub</span><span class="ysep">:</span> <span class="yurl"><a href="https://github.com/yaml/yamlstar">https://github.com/yaml/yamlstar</a></span>
  <span class="ykey">Documentation</span><span class="ysep">:</span> <span class="yurl"><a href="https://yamlstar.org/getting-started">https://yamlstar.org/getting-started</a></span>
  <span class="ykey">License</span><span class="ysep">:</span> <span class="ystr">MIT</span>
  <span class="ykey">Author</span><span class="ysep">:</span> <span class="ystr">Ingy döt Net</span>  <span class="ycom"># Inventor of YAML</span>
</pre>

<div class="home-cta">
  <a href="getting-started" class="cta-button">Get Started</a>
  <a href="bindings" class="cta-button-secondary">Language Bindings</a>
</div>

<div class="home-footer">
  <p>YAMLStar is built on the pure Clojure YAML Reference Parser.</p>
  <p>Created by Ingy döt Net, inventor of YAML and YAMLScript.</p>
</div>
