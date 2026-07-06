# YAMLStar Dart Binding

Dart binding for the YAMLStar shared library.

```dart
import 'package:yamlstar/yamlstar.dart';

final yaml = YAMLStar();
final data = yaml.load('key: value');
yaml.dispose();
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
```
