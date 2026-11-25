"""Start the packaged WAR with an unreachable database and check that failures are reported without leaking details."""
from pathlib import Path
import json
import os
import socket
import subprocess
import sys
import time
import unittest
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
LEAKS = ('jdbc', 'mysql', 'sqlexception', 'java.', 'communications link', 'connection refused', 'at br.uff', 'stack')


def free_port():
    with socket.socket() as listener:
        listener.bind(('127.0.0.1', 0))
        return listener.getsockname()[1]


def fetch(url):
    """Return (status, body) without following redirects, so flash messages and redirects stay visible."""
    class NoRedirect(urllib.request.HTTPRedirectHandler):
        def redirect_request(self, *args, **kwargs):
            return None

    opener = urllib.request.build_opener(NoRedirect)
    try:
        with opener.open(url, timeout=20) as response:
            return response.status, response.read().decode('utf-8', 'replace')
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode('utf-8', 'replace')


class InfrastructureFailureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.port = free_port()
        cls.base = f'http://127.0.0.1:{cls.port}'
        # Port 9 (discard) on loopback refuses connections, so every database call fails immediately.
        environment = dict(os.environ, MYSQL_CONNECTION_STRING='jdbc:mysql://127.0.0.1:9/e_store_unreachable',
                           MYSQL_USER='nobody', MYSQL_PASSWORD='nothing')
        java = environment.get('STORE_JAVA', 'java')
        command = [java, '-jar', 'target/dependency/webapp-runner.jar', '--port', str(cls.port), 'target/e-store-1.0.1.war']
        cls.log = (ROOT / 'target/infrastructure-failure-server.log').open('w', encoding='utf-8')
        cls.process = subprocess.Popen(command, cwd=ROOT, env=environment, stdout=cls.log, stderr=subprocess.STDOUT)
        deadline = time.monotonic() + 120
        while time.monotonic() < deadline:
            if cls.process.poll() is not None:
                raise RuntimeError('The server exited. Inspect target/infrastructure-failure-server.log.')
            try:
                status, _ = fetch(cls.base + '/api/products/')
                if status:
                    return
            except OSError:
                time.sleep(0.5)
        raise RuntimeError('The server did not become ready.')

    @classmethod
    def tearDownClass(cls):
        cls.process.terminate()
        try:
            cls.process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            cls.process.kill()
        cls.log.close()

    def assertNoLeak(self, text):
        lowered = text.lower()
        for marker in LEAKS:
            self.assertNotIn(marker, lowered, f'response leaked "{marker}"')

    def test_single_product_api_reports_a_generic_server_error(self):
        status, body = fetch(self.base + '/api/products/1')

        self.assertEqual(500, status)
        self.assertEqual({'error': 'The request could not be completed.'}, json.loads(body))
        self.assertNoLeak(body)

    def test_category_api_reports_a_generic_server_error(self):
        status, body = fetch(self.base + '/api/categories/')

        self.assertEqual(500, status)
        self.assertEqual({'error': 'The request could not be completed.'}, json.loads(body))
        self.assertNoLeak(body)

    def test_requests_that_need_no_database_are_not_affected(self):
        self.assertEqual(401, fetch(self.base + '/api/products/')[0])
        self.assertEqual(400, fetch(self.base + '/api/products/not-a-number')[0])

    def test_pages_fail_gracefully_without_stack_traces(self):
        for path in ('/products', '/product?productId=1', '/'):
            status, body = fetch(self.base + path)
            self.assertLess(status, 500, path)
            self.assertNoLeak(body)


if __name__ == '__main__':
    sys.exit(unittest.main())
