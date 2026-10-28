import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[1]


class CredentialLoginTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        self.runtime = (os.environ.get('YS_TEST_RUNTIME') or
                        os.environ.get('YS') or shutil.which('ys'))
        self.assertTrue(self.runtime, 'Run through the project Makes shell')
        self.secrets = self.directory / 'secrets.yaml'
        self.secrets.write_text(
            'moonbit:\n  uses: moon\n  user: fixture-user\n'
            '  token: fixture-old-token\n'
        )
        self.marker = self.directory / 'login-called'
        moon = self.directory / 'moon'
        moon.write_text('''#!/usr/bin/env python3
import os, sys
from pathlib import Path
assert sys.argv[1:] == ['login']
Path(os.environ['SECRETS_TEST_LOGIN_MARKER']).write_text('called')
print('fixture login stdout')
print('fixture login stderr', file=sys.stderr)
sys.exit(int(os.environ['SECRETS_TEST_LOGIN_EXIT']))
''')
        moon.chmod(0o755)
        source = (ROOT / 'util/yamlstar-secrets').read_text()
        fetch = "ys -le '.token:say' ~/.moon/credentials.json 2>/dev/null"
        self.assertIn(fetch, source)
        self.script = self.directory / 'yamlstar-secrets'
        self.script.write_text(source.replace(fetch, 'printf fixture-new-token'))
        self.env = dict(os.environ)
        self.env.update(
            YS_RELEASE_CI='1', YS_SECRETS_FILE=str(self.secrets),
            SECRETS_TEST_LOGIN_MARKER=str(self.marker),
            PATH=str(self.directory) + os.pathsep + self.env['PATH'],
        )

    def update(self, login_exit, ci=True):
        self.env['SECRETS_TEST_LOGIN_EXIT'] = str(login_exit)
        if not ci:
            self.env.pop('YS_RELEASE_CI', None)
        result = subprocess.run(
            [self.runtime, str(self.script), '--update=moonbit'],
            env=self.env, input='\n', capture_output=True, text=True,
            timeout=30, start_new_session=True,
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertTrue(self.marker.exists(), 'Login command did not run')
        self.assertIn('fixture login stdout', result.stdout)
        self.assertIn('fixture login stderr', result.stderr)
        self.assertIn('got token via login', result.stdout)
        self.assertIn('fixture-new-token', self.secrets.read_text())
        return result

    def test_successful_login_continues_to_token_fetch(self):
        result = self.update(0)
        self.assertNotIn('login command failed', result.stderr)

    def test_failed_login_warns_and_still_checks_for_a_token(self):
        result = self.update(7)
        self.assertIn('login command failed (7)', result.stderr)

    def test_login_without_a_controlling_terminal(self):
        self.update(0, ci=False)


if __name__ == '__main__':
    unittest.main()
