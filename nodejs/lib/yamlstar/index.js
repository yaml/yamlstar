const yamlstarVersion = '0.1.2';

const ffi = require('@makeomatic/ffi-napi');
const ref = require('ref-napi');
const path = require('path');
const fs = require('fs');
const os = require('os');

function defineForeignFunctionInterface() {
  let libPath = findLibyamlstarPath();

  return ffi.Library(libPath, {
    'graal_create_isolate': ['int', ['pointer', 'pointer', 'pointer']],
    'graal_tear_down_isolate': ['int', ['pointer']],
    'yamlstar_load': ['string', ['pointer', 'string']],
    'yamlstar_load_all': ['string', ['pointer', 'string']],
    'yamlstar_version': ['string', ['pointer']],
  });
}

class YAMLStar {
  constructor(config = {}) {
    this.libyamlstar = defineForeignFunctionInterface();
    this.isolatethread = ref.NULL_POINTER;

    let rc = this.libyamlstar.graal_create_isolate(
      null,
      null,
      this.isolatethread,
    );

    if (rc !== 0) {
      throw new Error('Failed to create GraalVM isolate');
    }
  }

  load(input) {
    let dataJson = this.libyamlstar.yamlstar_load(
      this.isolatethread.deref(),
      input,
    );

    let resp = JSON.parse(dataJson);

    if (resp.error) {
      throw new Error(`libyamlstar: ${resp.error.cause}`);
    }

    if (!('data' in resp)) {
      throw new Error("Unexpected response from 'libyamlstar'");
    }

    return resp.data;
  }

  loadAll(input) {
    let dataJson = this.libyamlstar.yamlstar_load_all(
      this.isolatethread.deref(),
      input,
    );

    let resp = JSON.parse(dataJson);

    if (resp.error) {
      throw new Error(`libyamlstar: ${resp.error.cause}`);
    }

    if (!('data' in resp)) {
      throw new Error("Unexpected response from 'libyamlstar'");
    }

    return resp.data;
  }

  version() {
    return this.libyamlstar.yamlstar_version(this.isolatethread.deref());
  }

  close() {
    let ret = this.libyamlstar.graal_tear_down_isolate(
      this.isolatethread.deref(),
    );

    if (ret !== 0) {
      throw new Error("Failed to tear down isolate.");
    }
  }
}

// Helper function to find the libyamlstar shared library path
function findLibyamlstarPath() {
  let platform = os.platform();
  let soExtension = platform === 'win32' ? 'dll' : (platform === 'linux' ? 'so' : 'dylib');
  let libyamlstarName = `libyamlstar.${soExtension}.${yamlstarVersion}`;

  let searchPaths = [];

  // Add relative path for development (from nodejs/lib/yamlstar to libyamlstar/lib)
  let devPath = path.join(__dirname, '..', '..', '..', 'libyamlstar', 'lib');
  if (fs.existsSync(devPath)) {
    searchPaths.push(devPath);
  }

  // Add LD_LIBRARY_PATH
  if (process.env.LD_LIBRARY_PATH) {
    searchPaths.push(...process.env.LD_LIBRARY_PATH.split(':'));
  }

  // Add system paths
  searchPaths.push('/usr/local/lib', path.join(os.homedir(), '.local', 'lib'));

  for (let p of searchPaths) {
    let fullPath = path.join(p, libyamlstarName);
    if (fs.existsSync(fullPath)) {
      return fullPath;
    }
  }

  throw new Error(
`Shared library file '${libyamlstarName}' not found
Search paths: ${searchPaths.join(':')}
Build with: cd libyamlstar && make native`);
}

module.exports = YAMLStar;
