"""Download portable Java 8 and Maven for local verification without system installation."""
from pathlib import Path
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
TOOLS = ROOT / '.local-tools'


def download(url, destination):
    if not destination.exists():
        print('Downloading ' + destination.name, flush=True)
        request = urllib.request.Request(url, headers={'User-Agent': 'e-store-development'})
        with urllib.request.urlopen(request, timeout=60) as response, destination.open('wb') as output:
            while chunk := response.read(1024 * 1024):
                output.write(chunk)


def main():
    TOOLS.mkdir(exist_ok=True)
    if not list(TOOLS.glob('jdk*/bin/java.exe')):
        archive = TOOLS / 'java8.zip'
        download('https://api.adoptium.net/v3/binary/latest/8/ga/windows/x64/jdk/hotspot/normal/eclipse', archive)
        with zipfile.ZipFile(archive) as contents:
            contents.extractall(TOOLS)
    if not (TOOLS / 'apache-maven-3.9.9/bin/mvn.cmd').exists():
        archive = TOOLS / 'maven.zip'
        download('https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip', archive)
        with zipfile.ZipFile(archive) as contents:
            contents.extractall(TOOLS)
    print('Portable tooling is ready in .local-tools.', flush=True)


if __name__ == '__main__':
    main()
