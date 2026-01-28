---
hide:
  - navigation
  - toc
---

<div class="home-hero">
  <h1 class="home-title">
    <span class="star-icon">★</span> YAMLStar
  </h1>
  <p class="home-subtitle">A YAML framework for all programming languages</p>
</div>

<pre class="home-page">
<span class="ykey">What Is YAMLStar</span><span class="ysep">:</span>
  <span class="ykey">Description</span><span class="ysep">:</span> <span class="ystr">Pure YAML 1.2 loader with cross-language consistency</span>
  <span class="ykey">Version</span><span class="ysep">:</span> <span class="yver">0.1.2</span>
  <span class="ykey">Languages</span><span class="ysep">:</span>
    - <span class="ystr">Clojure</span>
    - <span class="ystr">C#</span>
    - <span class="ystr">Delphi</span>
    - <span class="ystr">Fortran</span>
    - <span class="ystr">Go</span>
    - <span class="ystr">Java</span>
    - <span class="ystr">Node.js</span>
    - <span class="ystr">Perl</span>
    - <span class="ystr">Python</span>
    - <span class="ystr">Rust</span>
  <span class="ykey">Features</span><span class="ysep">:</span>
    <span class="ykey">Spec Compliant</span><span class="ysep">:</span> <span class="ybool">true</span>   <span class="ycom"># 100% YAML 1.2 specification</span>
    <span class="ykey">Pure Clojure</span><span class="ysep">:</span> <span class="ybool">true</span>    <span class="ycom"># No SnakeYAML dependencies</span>
    <span class="ykey">Lightweight</span><span class="ysep">:</span> <span class="ybool">true</span>     <span class="ycom"># Minimal dependencies</span>
    <span class="ykey">Extensible</span><span class="ysep">:</span> <span class="ybool">true</span>      <span class="ycom"># Plugin system (Phase 3)</span>

<span class="ykey">Quick Start</span><span class="ysep">:</span>
  <span class="ykey">Python</span><span class="ysep">:</span> |
    <span class="ycom"># pip install yamlstar</span>
    <span class="ykw">from</span> yamlstar <span class="ykw">import</span> YAMLStar

    ys = YAMLStar()
    data = ys.load(<span class="ystr">'key: value'</span>)
    <span class="ycom"># {'key': 'value'}</span>
    ys.close()

  <span class="ykey">Node.js</span><span class="ysep">:</span> |
    <span class="ycom">// npm install yamlstar</span>
    <span class="ykw">const</span> YAMLStar = require(<span class="ystr">'yamlstar'</span>);

    <span class="ykw">const</span> ys = <span class="ykw">new</span> YAMLStar();
    <span class="ykw">const</span> data = ys.load(<span class="ystr">'key: value'</span>);
    <span class="ycom">// { key: 'value' }</span>
    ys.close();

  <span class="ykey">Clojure</span><span class="ysep">:</span> |
    <span class="ycom">; lein/deps.edn: org.yamlstar/yamlstar "0.1.2"</span>
    (<span class="ykw">require</span> '[yamlstar.core <span class="ykw">:as</span> yaml])

    (yaml/load <span class="ystr">"key: value"</span>)
    <span class="ycom">;=> {"key" "value"}</span>

  <span class="ykey">Go</span><span class="ysep">:</span> |
    <span class="ycom">// go get github.com/yaml/yamlstar-go</span>
    <span class="ykw">import</span> <span class="ystr">"github.com/yaml/yamlstar-go"</span>

    ys := yamlstar.New()
    data := ys.Load(<span class="ystr">"key: value"</span>)
    <span class="ycom">// map[string]interface{}{"key": "value"}</span>
    ys.Close()

<span class="ykey">Architecture</span><span class="ysep">:</span>
  <span class="ykey">Pipeline</span><span class="ysep">:</span>
    - <span class="ykey">Parser</span><span class="ysep">:</span> <span class="ystr">Pure Clojure YAML 1.2 parser</span>
    - <span class="ykey">Composer</span><span class="ysep">:</span> <span class="ystr">Event stream to node tree</span>
    - <span class="ykey">Resolver</span><span class="ysep">:</span> <span class="ystr">Type inference (!!str, !!int, !!bool, etc.)</span>
    - <span class="ykey">Constructor</span><span class="ysep">:</span> <span class="ystr">Nodes to native data structures</span>
  <span class="ykey">Dependencies</span><span class="ysep">:</span> <span class="ynum">0</span>  <span class="ycom"># Zero external dependencies</span>

<span class="ykey">Resources</span><span class="ysep">:</span>
  <span class="ykey">GitHub</span><span class="ysep">:</span> <span class="yurl">https://github.com/yaml/yamlstar</span>
  <span class="ykey">Documentation</span><span class="ysep">:</span> <span class="yurl">https://yamlstar.org/getting-started</span>
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
