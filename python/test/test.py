import pytest

def modules_compile():
    import yamlstar
    return "ok"

def test_modules_compile():
    assert modules_compile() == "ok"
