"""Browser and live-container security smoke tests; run against a disposable local app."""
import os
import unittest

from playwright.sync_api import sync_playwright


class StoreBrowserSmokeTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.playwright = sync_playwright().start()
        channel = os.environ.get('STORE_BROWSER_CHANNEL')
        cls.browser = cls.playwright.chromium.launch(headless=True, channel=channel)
        cls.base_url = os.environ.get('STORE_BASE_URL', 'http://localhost:8181')

    @classmethod
    def tearDownClass(cls):
        cls.browser.close()
        cls.playwright.stop()

    def setUp(self):
        self.context = self.browser.new_context(base_url=self.base_url)
        self.page = self.context.new_page()

    def tearDown(self):
        self.context.close()

    def test_login_forms_render_session_bound_tokens(self):
        response = self.page.goto('/account')
        self.assertEqual(200, response.status)
        tokens = self.page.locator('form input[name="_csrf"]').evaluate_all(
            '(inputs) => inputs.map(input => input.value)')
        self.assertGreaterEqual(len(tokens), 4)
        self.assertTrue(all(len(token) == 43 for token in tokens))
        self.assertEqual(1, len(set(tokens)))
        self.assertEqual('no-store', response.headers.get('cache-control'))

    def test_missing_and_foreign_tokens_cannot_change_locale(self):
        self.page.goto('/account')
        for form in [{'locale': 'es'}, {'locale': 'es', '_csrf': 'foreign-session-token'}]:
            response = self.context.request.post('/language', form=form)
            self.assertEqual(403, response.status)
        self.page.reload()
        self.assertEqual('en', self.page.locator('html').get_attribute('lang'))

    def test_valid_token_changes_locale_in_the_same_session(self):
        self.page.goto('/account')
        token = self.page.locator('input[name="_csrf"]').first.input_value()
        # Avoid following the home redirect: this smoke suite does not require MySQL.
        response = self.context.request.post('/language', form={'locale': 'es', '_csrf': token},
                                             max_redirects=0)
        self.assertEqual(302, response.status)
        self.page.goto('/account')
        self.assertEqual('es', self.page.locator('html').get_attribute('lang'))
        self.assertEqual('Idioma', self.page.locator('label[for="locale"]').inner_text())

    def test_all_four_locales_translate_the_sign_in_action(self):
        self.page.goto('/account')
        token = self.page.locator('input[name="_csrf"]').first.input_value()
        for locale, text in [('en', 'Sign in'), ('pt-BR', 'Entrar'), ('es', 'Entrar'), ('pt-PT', 'Iniciar sessão')]:
            response = self.context.request.post('/language', form={'locale': locale, '_csrf': token}, max_redirects=0)
            self.assertEqual(302, response.status)
            self.page.goto('/account')
            self.assertEqual(locale, self.page.locator('html').get_attribute('lang'))
            self.assertEqual(text, self.page.locator('button[value="login"]').inner_text())

    def test_token_cannot_be_reused_in_another_browser_session(self):
        self.page.goto('/account')
        token = self.page.locator('input[name="_csrf"]').first.input_value()
        other = self.browser.new_context(base_url=self.base_url)
        try:
            other.new_page().goto('/account')
            response = other.request.post('/language', form={'locale': 'es', '_csrf': token})
            self.assertEqual(403, response.status)
        finally:
            other.close()

    def test_swagger_loads_local_schemas_and_guest_account_reads_are_denied(self):
        self.page.goto('/swagger/')
        self.page.locator('.swagger-ui .opblock').first.wait_for()
        self.assertEqual(5, self.page.locator('.swagger-ui .opblock').count())
        response = self.context.request.get('/api/users/')
        self.assertEqual(401, response.status)
        self.assertEqual({'error': 'Authentication is required.'}, response.json())

    def test_untrusted_redirect_cannot_inject_markup_into_login_forms(self):
        payload = '\"><img src=x onerror="window.injected=true">'
        response = self.context.request.get('/account', params={'redirect': payload})
        self.assertEqual(200, response.status)
        self.assertNotIn(payload, response.text())
        self.page.goto('/account?redirect=https%3A%2F%2Fexample.test')
        self.assertEqual('', self.page.locator('input[name="redirect"]').first.input_value())
        self.assertTrue(self.page.url.startswith(self.base_url))


if __name__ == '__main__':
    unittest.main()
