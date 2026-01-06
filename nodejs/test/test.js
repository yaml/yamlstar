const YAMLStar = require('../lib/yamlstar');
const assert = require('assert');

// Helper function for deep equality check
function deepEqual(actual, expected, message) {
  try {
    assert.deepStrictEqual(actual, expected);
    console.log(`✓ ${message}`);
  } catch (e) {
    console.error(`✗ ${message}`);
    console.error(`  Expected: ${JSON.stringify(expected)}`);
    console.error(`  Actual:   ${JSON.stringify(actual)}`);
    process.exit(1);
  }
}

// Create a YAMLStar instance
const ys = new YAMLStar();

console.log('Running YAMLStar tests...\n');

// Test simple scalar
deepEqual(ys.load('hello'), 'hello', 'Load simple scalar');

// Test integer
deepEqual(ys.load('42'), 42, 'Load integer');

// Test float
deepEqual(ys.load('3.14'), 3.14, 'Load float');

// Test boolean true
deepEqual(ys.load('true'), true, 'Load boolean true');

// Test boolean false
deepEqual(ys.load('false'), false, 'Load boolean false');

// Test null
deepEqual(ys.load('null'), null, 'Load null');

// Test simple mapping
deepEqual(ys.load('key: value'), {key: 'value'}, 'Load simple mapping');

// Test nested mapping
deepEqual(
  ys.load('outer:\n  inner: value'),
  {outer: {inner: 'value'}},
  'Load nested mapping'
);

// Test mapping with multiple keys
deepEqual(
  ys.load('key1: value1\nkey2: value2\nkey3: value3'),
  {key1: 'value1', key2: 'value2', key3: 'value3'},
  'Load mapping with multiple keys'
);

// Test simple sequence
deepEqual(
  ys.load('- item1\n- item2\n- item3'),
  ['item1', 'item2', 'item3'],
  'Load simple sequence'
);

// Test flow sequence
deepEqual(ys.load('[a, b, c]'), ['a', 'b', 'c'], 'Load flow sequence');

// Test type coercion
deepEqual(
  ys.load('string: hello\ninteger: 42\nfloat: 3.14\nbool_true: true\nbool_false: false\nnull_value: null'),
  {
    string: 'hello',
    integer: 42,
    float: 3.14,
    bool_true: true,
    bool_false: false,
    null_value: null
  },
  'Load with type coercion'
);

// Test sequence of mappings
deepEqual(
  ys.load('- name: Alice\n  age: 30\n- name: Bob\n  age: 25'),
  [
    {name: 'Alice', age: 30},
    {name: 'Bob', age: 25}
  ],
  'Load sequence of mappings'
);

// Test mapping with sequence values
deepEqual(
  ys.load('fruits:\n  - apple\n  - banana\ncolors:\n  - red\n  - blue'),
  {
    fruits: ['apple', 'banana'],
    colors: ['red', 'blue']
  },
  'Load mapping with sequence values'
);

// Test loadAll with single document
deepEqual(ys.loadAll('hello'), ['hello'], 'LoadAll with single document');

// Test loadAll with multiple documents
deepEqual(
  ys.loadAll('---\ndoc1\n---\ndoc2\n---\ndoc3'),
  ['doc1', 'doc2', 'doc3'],
  'LoadAll with multiple documents'
);

// Test loadAll with explicit markers
deepEqual(
  ys.loadAll('---\na: 1\n...\n---\nb: 2\n...'),
  [{a: 1}, {b: 2}],
  'LoadAll with explicit markers'
);

// Test version
const version = ys.version();
assert(typeof version === 'string' && version.length > 0, 'Version returns a string');
console.log(`✓ Version returns a string: ${version}`);

// Test error handling with malformed YAML
try {
  ys.load('key: "unclosed');
  console.error('✗ Should have thrown error on malformed YAML');
  process.exit(1);
} catch (e) {
  assert(e.message.includes('libyamlstar'), 'Error message includes libyamlstar');
  console.log('✓ Dies with libyamlstar error on malformed YAML');
}

// Test empty document
deepEqual(ys.load(''), null, 'Load empty document');

// Test whitespace only
deepEqual(ys.load('   \n  \n  '), null, 'Load whitespace only');

// Test quoted strings
deepEqual(ys.load("'hello world'"), 'hello world', 'Load single-quoted string');
deepEqual(ys.load('"hello world"'), 'hello world', 'Load double-quoted string');

// Clean up
ys.close();

console.log('\n✓ All tests passed!');
