#!/usr/bin/env python3
"""Run with: python yaml-to-json.py [input.yaml]"""

import sys
import json

# Add parent python/lib to path for development
sys.path.insert(0, '../../python/lib')

import yamlstar

def main(args):
    yaml_file = args[0] if args else "sample.yaml"
    print(f"YAMLStar Example - Loading {yaml_file} and outputting JSON\n")

    with open(yaml_file) as f:
        yaml_content = f.read()

    print("Input YAML:")
    print(yaml_content)
    print("\n---\n")

    ys = yamlstar.YAMLStar()
    data = ys.load(yaml_content)

    json_output = json.dumps(data, indent=2)

    print("Output JSON:")
    print(json_output)

if __name__ == '__main__':
    main(sys.argv[1:])
