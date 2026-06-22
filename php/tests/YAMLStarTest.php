<?php

require_once __DIR__ . '/../src/YAMLStar.php';

use YAMLStar\YAMLStar;

function assertSameValue($expected, $actual, string $message): void
{
    if ($expected !== $actual) {
        fwrite(STDERR, "not ok - $message\n");
        fwrite(STDERR, 'expected: ' . var_export($expected, true) . "\n");
        fwrite(STDERR, 'actual:   ' . var_export($actual, true) . "\n");
        exit(1);
    }
    echo "ok - $message\n";
}

$ys = new YAMLStar();

assertSameValue(['key' => 'value'], $ys->load('key: value'), 'load mapping');
assertSameValue(['a', 'b'], $ys->load("- a\n- b\n"), 'load sequence');
assertSameValue(['doc1', ['a' => 1]], $ys->loadAll("---\ndoc1\n---\na: 1\n"), 'load all');
assertSameValue("key: value\n", $ys->dump(['key' => 'value']), 'dump mapping');
assertSameValue(
    ['items' => ['a', 'b'], 'flag' => true, 'text' => '42'],
    $ys->load($ys->dump(['items' => ['a', 'b'], 'flag' => true, 'text' => '42'])),
    'dump round trip'
);
assertSameValue("---\ndoc1\n---\na: 1\n", $ys->dumpAll(['doc1', ['a' => 1]]), 'dump all');

if (!preg_match('/^\d+\.\d+\.\d+(-SNAPSHOT)?$/', $ys->version())) {
    fwrite(STDERR, "not ok - version\n");
    exit(1);
}
echo "ok - version\n";

try {
    $ys->load('key: "unclosed');
    fwrite(STDERR, "not ok - malformed YAML should fail\n");
    exit(1);
} catch (RuntimeException $e) {
    if (!str_contains($e->getMessage(), 'libyamlstar:')) {
        throw $e;
    }
    echo "ok - malformed YAML error\n";
}

$ys->close();
