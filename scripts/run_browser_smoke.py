"""Start a disposable servlet-container process and run browser security smoke tests."""
from pathlib import Path
import os
import socket
import subprocess
import sys
import time
import urllib.error
import urllib.request
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[1]


def main():
    with socket.socket() as listener:
        listener.bind(('127.0.0.1', 0))
        port = listener.getsockname()[1]
    environment = dict(os.environ, STORE_BASE_URL=f'http://127.0.0.1:{port}')
    if environment.get('STORE_DATABASE_E2E') == '1':
        database_url = environment.get('MYSQL_CONNECTION_STRING', '')
        if not database_url.startswith('jdbc:') or urlsplit(database_url[5:]).path != '/e_store_integration':
            raise RuntimeError('Database E2E requires the disposable e_store_integration schema.')
    java = environment.get('STORE_JAVA', 'java')
    command = [java, '-jar', 'target/dependency/webapp-runner.jar', '--port', str(port),
               'target/e-store-1.0.1.war']
    with (ROOT / 'target/browser-server.log').open('w', encoding='utf-8') as output:
        process = subprocess.Popen(command, cwd=ROOT, stdout=output, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 120
            while time.monotonic() < deadline:
                if process.poll() is not None:
                    raise RuntimeError('The server exited. Inspect target/browser-server.log.')
                try:
                    with urllib.request.urlopen(environment['STORE_BASE_URL'] + '/account', timeout=2) as response:
                        if response.status == 200:
                            break
                except (OSError, urllib.error.URLError):
                    time.sleep(0.5)
            else:
                raise RuntimeError('The server did not become ready. Inspect target/browser-server.log.')
            result = subprocess.run([sys.executable, 'scripts/test_browser_smoke.py'], cwd=ROOT,
                                    env=environment, timeout=180)
            if result.returncode or environment.get('STORE_DATABASE_E2E') != '1':
                return result.returncode
            for script in ('scripts/test_browser_store.py', 'scripts/test_browser_hostile.py'):
                result = subprocess.run([sys.executable, script], cwd=ROOT, env=environment, timeout=300)
                if result.returncode:
                    return result.returncode
            return 0
        finally:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()


if __name__ == '__main__':
    sys.exit(main())
