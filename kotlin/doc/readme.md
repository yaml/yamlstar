## Kotlin Usage

Use `kotlin-yamlstar` as a drop-in replacement for your current
YAML loader:

File `main.kt`:

```kotlin
import java.io.File
import org.yamlstar.yamlstar.YS

fun main() {
    val data = YS.loadObject(File("config.yaml").readText())
    println(data.toString(2))
}
```


## Installation

Add the `kotlin-yamlstar` artifact to your project and install the
`libyamlstar.so` shared library:

```kotlin
// build.gradle.kts
repositories {
    maven("https://repo.clojars.org")
}
dependencies {
    implementation("org.yamlstar:kotlin-yamlstar:0.1.16")
}
```

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
export LD_LIBRARY_PATH="$HOME/.local/lib:$LD_LIBRARY_PATH"
```

See <https://yamlstar.org/doc/install/> for more info.


### Requirements

* JDK 8 or higher
* Linux, macOS or Windows
