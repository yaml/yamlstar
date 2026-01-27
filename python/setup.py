#!/usr/bin/env python3

from setuptools import setup
import re
from pathlib import Path

# Read version from ../Meta file
meta_file = Path(__file__).parent.parent / 'Meta'
meta_content = meta_file.read_text()
match = re.search(r'^version:\s*(.+)$', meta_content, re.MULTILINE)
version = match.group(1) if match else None

# Validate version format
if not version or not re.match(r'^\d+\.\d+\.\d+', version):
    raise ValueError(f"Invalid or missing version in Meta file: {version}")

setup(
    name = 'yamlstar',
    version = version,
    description = 'Python bindings for YAMLStar - YAML 1.2 loader',
    long_description = open('ReadMe.md').read() if __name__ == '__main__' else '',
    long_description_content_type = 'text/markdown',
    author = 'Ingy döt Net',
    author_email = 'ingy@ingy.net',
    url = 'https://github.com/yaml/yamlstar',
    license = 'MIT',
    packages = ['yamlstar'],
    package_dir = {'': 'lib'},
    python_requires = '>=3.6, <4',
    install_requires = [],
    setup_requires = ['wheel'],
    classifiers = [
        'Development Status :: 3 - Alpha',
        'Intended Audience :: Developers',
        'License :: OSI Approved :: MIT License',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.6',
        'Programming Language :: Python :: 3.7',
        'Programming Language :: Python :: 3.8',
        'Programming Language :: Python :: 3.9',
        'Programming Language :: Python :: 3.10',
        'Programming Language :: Python :: 3.11',
        'Programming Language :: Python :: 3.12',
        'Topic :: Software Development :: Libraries',
    ],
)
