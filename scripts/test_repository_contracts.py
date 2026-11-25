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

    def test_visitor_facing_page_text_is_localized_not_hard_coded(self):
        patterns = {
            'literal alert': r'alert\("[^"$]+"\)',
            'literal confirm': r"confirm\('[^'$<]+'\)",
            'literal page title': r'<jsp:param name="title" value="[A-Za-z][^"$<]*"',
            'literal placeholder': r'placeholder="[A-Za-z][^"$<]*"',
        }
        for page in (ROOT / 'src/main/webapp/pages').glob('*.jsp'):
            source = page.read_text(encoding='utf-8')
            for label, pattern in patterns.items():
                self.assertIsNone(re.search(pattern, source), f'{page.name}: {label}')

    def test_form_controls_have_an_accessible_name_and_labels_point_at_real_controls(self):
        tag = r'<(?:input|textarea|select)\b(?:<%.*?%>|[^>])*>'
        skipped_types = ('hidden', 'submit', 'button', 'radio', 'checkbox', 'range', 'image')
        for page in (ROOT / 'src/main/webapp/pages').glob('*.jsp'):
            source = page.read_text(encoding='utf-8')
            ids = set(re.findall(r'\bid="([^"]+)"', source))
            for target in re.findall(r'<label\b[^>]*\bfor="([^"$<]+)"', source):
                self.assertIn(target, ids, f'{page.name}: label points at missing id {target}')
            labelled = set(re.findall(r'<label\b[^>]*\bfor="([^"]+)"', source))
            for control in re.findall(tag, source, re.S):
                kind = re.search(r'\btype="([^"]+)"', control)
                if (kind and kind.group(1) in skipped_types) or 'display:none' in control:
                    continue
                identifier = re.search(r'\bid="([^"]+)"', control)
                named = 'aria-label=' in control or 'aria-labelledby=' in control
                associated = identifier is not None and identifier.group(1) in labelled
                self.assertTrue(named or associated, f'{page.name}: unlabeled control {control[:90]}')

    def test_password_fields_enforce_the_documented_minimum_length_in_the_browser(self):
        for page in ('login.jsp', 'account-edit.jsp'):
            source = (ROOT / 'src/main/webapp/pages' / page).read_text(encoding='utf-8')
            new_password = re.search(r'<input[^>]*autocomplete="new-password"[^>]*>', source).group(0)
            self.assertIn('minlength="8"', new_password, page)

    def test_core_does_not_know_the_shape_of_database_rows(self):
        for source in (ROOT / 'src/main/java/br/uff/store/core').rglob('*.java'):
            self.assertNotIn('Map<String, Object>', source.read_text(encoding='utf-8'), str(source))

    def test_renamed_legacy_identifiers_do_not_return(self):
        legacy = re.compile(r'(avaliacoes|espParameter|durMes|ckEmail|lancs|MAXPAGSTR|COUNTSTRING)')
        for source in list((ROOT / 'src').rglob('*.java')) + list((ROOT / 'src/main/webapp').rglob('*.jsp')):
            self.assertIsNone(legacy.search(source.read_text(encoding='utf-8')), str(source))

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
        sources = list((ROOT / 'src').rglob('*.java')) + list((ROOT / 'src/main/webapp').rglob('*.jsp'))
        for source in sources:
            text = source.read_text(encoding='utf-8')
            for imported in re.findall(r'import[= ]"?(br\.uff\.store\.[\w.]+)', text):
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

    def test_browser_forms_include_the_csrf_fragment_inside_the_form(self):
        fragment = '<%@ include file="/WEB-INF/fragments/csrf.jspf" %>'
        pages = ROOT / 'src/main/webapp/pages'
        for page in pages.glob('*.jsp'):
            text = page.read_text(encoding='utf-8')
            # Scriptlets can contain > characters inside quoted form attributes.
            normalized = re.sub(r'<%(?!@).*?%>', 'expression', text, flags=re.S)
            forms = re.findall(r'<form\b[^>]*>(.*?)</form>', normalized, flags=re.S | re.I)
            self.assertEqual(len(re.findall(r'<form\b', normalized)), len(forms), str(page))
            for form in forms:
                self.assertIn(fragment, form, str(page))
        payment = (pages / 'payment.jsp').read_text(encoding='utf-8')
        if 'document.createElement("form")' in payment:
            self.assertIn('purchaseForm.appendChild(csrfInput)', payment)

    def test_csrf_filter_covers_all_browser_controller_routes(self):
        filter_path = ROOT / 'src/main/java/br/uff/store/application/security/CsrfFilter.java'
        text = filter_path.read_text(encoding='utf-8')
        patterns = re.search(r'@WebFilter\(urlPatterns = \{(.*?)\}\)', text, re.S).group(1)
        protected = set(re.findall(r'"([^"]+)"', patterns))
        for source in (ROOT / 'src/main/java/br/uff/store/application/controllers').glob('*.java'):
            for route in re.findall(r'@WebServlet\("([^"]*)"\)', source.read_text(encoding='utf-8')):
                self.assertIn(route or '/', protected, str(source))
        self.assertIn('/pages/*', protected)

    def test_caught_exceptions_have_descriptive_variable_names(self):
        abbreviated = re.compile(r'catch \([\w. |]+? (?:e|ex|el|e1|e2|exc|ie|nfe)\)')
        for source in list((ROOT / 'src').rglob('*.java')):
            self.assertNotRegex(source.read_text(encoding='utf-8'), abbreviated, str(source))

    def test_starting_the_store_never_runs_tests_and_has_a_separate_test_script(self):
        compose = (ROOT / 'docker-compose.yml').read_text(encoding='utf-8')
        self.assertIn('-DskipTests', compose)
        start = (ROOT / 'scripts/start.sh').read_text(encoding='utf-8')
        self.assertNotRegex(start, r'mvn|pytest|unittest')
        self.assertTrue((ROOT / 'scripts/test.sh').exists())
        self.assertIn('${MYSQL_PORT:-3316}', compose)
        self.assertIn('MYSQL_PORT=3316', (ROOT / '.env.example').read_text(encoding='utf-8'))

    def test_every_demo_adapter_is_listed_as_not_production_ready(self):
        document = (ROOT / 'docs/non-production-readiness.md').read_text(encoding='utf-8')
        adapters = list((ROOT / 'src/main/java/br/uff/store/infrastructure/gateways').glob('*.java'))
        self.assertTrue(adapters)
        for adapter in adapters:
            self.assertIn(adapter.stem, document, f'{adapter.name} must be listed in docs/non-production-readiness.md')

    def test_stylesheets_never_remove_the_keyboard_focus_ring(self):
        for sheet in (ROOT / 'src/main/webapp/css').glob('*.css'):
            self.assertNotRegex(sheet.read_text(encoding='utf-8'), r'outline\s*:\s*(?:none|0)\s*[;}]', str(sheet))

    def test_core_cannot_import_outer_layers_or_frameworks(self):
        forbidden = re.compile(r'import (?:br\.uff\.store\.(?:application|infrastructure)|'
                               r'javax\.servlet|com\.google\.gson)')
        for source in (ROOT / 'src/main/java/br/uff/store/core').rglob('*.java'):
            self.assertNotRegex(source.read_text(encoding='utf-8'), forbidden, str(source))


if __name__ == '__main__':
    unittest.main()
