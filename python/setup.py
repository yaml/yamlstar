#!/usr/bin/env python3

from setuptools import setup
import re
from pathlib import Path

# Read version from ../Meta file
meta_file = Path(__file__).parent.parent / 'Meta'
if meta_file.exists():
    meta_content = meta_file.read_text()
    match = re.search(r'^version:\s*(.+)$', meta_content, re.MULTILINE)
    version = match.group(1) if match else '0.1.0'
else:
    version = '0.1.0'

setup(
    name = 'yamlstar',
    version = version,
    description = 'Python bindings for YAMLStar - Pure YAML 1.2 loader',
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
