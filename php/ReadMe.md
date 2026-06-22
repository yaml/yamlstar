# YAMLStar PHP Binding

PHP binding for the YAMLStar shared library.

```php
<?php
require 'vendor/autoload.php';

$ys = new YAMLStar\YAMLStar();
$data = $ys->load("key: value");
$text = $ys->dump(["foo" => [["bar"]]]);
$ys->close();
```

Install the matching `libyamlstar` release before using this package:

```bash
curl -sSL https://yamlstar.org/install | LIB=1 bash
composer require yaml/yamlstar-php
```

