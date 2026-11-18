"""Checks that can run without the Java or Docker toolchains."""
import base64
import hashlib
import json
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]


class RepositoryContractsTest(unittest.TestCase):
    def test_schema_creates_dependencies_before_foreign_keys(self):
        schema = (ROOT / 'database/migrations/V1__create_tables.sql').read_text()
        created = set()
        for table, definition in re.findall(r'CREATE TABLE `([^`]+)` (.*?;)\s', schema, re.S):
            for dependency in re.findall(r'REFERENCES `([^`]+)`', definition):
                self.assertTrue(dependency == table or dependency in created, (table, dependency))
            created.add(table)
        self.assertEqual(10, len(created))
        self.assertNotIn('DROP TABLE', schema)

    def test_local_accounts_use_verifiable_password_hashes(self):
        seeds = (ROOT / 'database/dev-seeds/V5__seed_development_store.sql').read_text()
        hashes = re.findall(r'\$pbkdf2-sha256\$\d+\$[\w-]+\$[\w-]+', seeds)
        self.assertEqual(2, len(hashes))
        for encoded in hashes:
            _, _, rounds, salt, key = encoded.split('$')
            decode = lambda value: base64.urlsafe_b64decode(value + '=' * (-len(value) % 4))
            self.assertGreaterEqual(len(decode(salt)), 16)
            actual = hashlib.pbkdf2_hmac('sha256', b'development-password', decode(salt), int(rounds))
            self.assertEqual(decode(key), actual)

    def test_servlet_view_destinations_exist(self):
        for source in (ROOT / 'src/main/java').rglob('*.java'):
            for view in re.findall(r'"(pages/[^"?]+\.jsp)"', source.read_text(encoding='utf-8')):
                self.assertTrue((ROOT / 'src/main/webapp' / view).is_file(), (source, view))

    def test_public_routes_are_english(self):
        legacy = r'(?:produto|produtos|usuario|endereco|carrinho|compra|avaliacao|categoria)(?:[/?]|$)'
        for source in (ROOT / 'src/main/java').rglob('*.java'):
            for route in re.findall(r'@WebServlet\("([^"]+)"\)', source.read_text(encoding='utf-8')):
                self.assertNotRegex(route.lstrip('/').removeprefix('api/'), legacy)

    def test_java_files_stay_below_300_lines(self):
        for source in (ROOT / 'src').rglob('*.java'):
            self.assertLess(len(source.read_text(encoding='utf-8').splitlines()), 300, str(source))

    def test_java_types_match_their_english_filenames(self):
        legacy = ('Produto', 'Produtos', 'Carrinho', 'Usuario', 'Endereco', 'Categoria', 'Avaliacao', 'Venda', 'Compra', 'Loja')
        for source in (ROOT / 'src').rglob('*.java'):
            self.assertFalse(any(word in source.stem for word in legacy), str(source))
            text = source.read_text(encoding='utf-8')
            declarations = re.findall(r'(?:public )?(?:final |abstract )?(?:class|interface|enum) (\w+)', text)
            self.assertIn(source.stem, declarations, str(source))
            package = re.search(r'package ([\w.]+);', text).group(1)
            self.assertTrue(source.parent.as_posix().endswith(package.replace('.', '/')), str(source))

    def test_internal_java_imports_resolve_after_renaming(self):
        for source in (ROOT / 'src').rglob('*.java'):
            text = source.read_text(encoding='utf-8')
            for imported in re.findall(r'import (br\.uff\.store\.[\w.]+);', text):
                relative = imported.replace('.', '/') + '.java'
                self.assertTrue(any((ROOT / folder / relative).is_file()
                                    for folder in ['src/main/java', 'src/test/java']), imported)

    def test_stylesheet_links_resolve_after_renaming(self):
        for page in (ROOT / 'src/main/webapp/pages').glob('*.jsp'):
            for style in re.findall(r'href="(css/[^"?]+\.css)"', page.read_text(encoding='utf-8')):
                self.assertTrue((ROOT / 'src/main/webapp' / style).is_file(), style)

    def test_openapi_covers_the_read_api_and_protects_user_routes(self):
        spec = json.loads((ROOT / 'src/main/webapp/swagger/openapi.json').read_text())
        self.assertEqual('3.0.3', spec['openapi'])
        self.assertEqual({'/api/products/', '/api/products/{productId}', '/api/categories/',
                          '/api/users/', '/api/users/{userId}'}, set(spec['paths']))
        for path, operations in spec['paths'].items():
            self.assertEqual({'get'}, set(operations))
            if path.startswith('/api/users'):
                self.assertEqual([{'sessionCookie': []}], operations['get']['security'])
                self.assertIn('401', operations['get']['responses'])
                self.assertIn('403', operations['get']['responses'])
        for name, reference in spec['components']['schemas'].items():
            path = ROOT / 'src/main/webapp/swagger' / reference['$ref']
            schema = json.loads(path.read_text())
            self.assertNotIn('senha', schema['properties'], name)
            self.assertNotIn('password', schema['properties'], name)


if __name__ == '__main__':
    unittest.main()
