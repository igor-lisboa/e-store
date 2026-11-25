"""Browser journey for password recovery: request a link, read the demo outbox, set a new password, reuse is refused."""
import glob
import os
import re
import unittest
import uuid

from playwright.sync_api import sync_playwright

OUTBOX = os.environ.get('EMAIL_OUTBOX_DIR', '')


def message_for(email):
    """Return the text of the newest demo email addressed to the given address."""
    newest = None
    for path in sorted(glob.glob(os.path.join(OUTBOX, '*.txt'))):
        with open(path, encoding='utf-8') as handle:
            text = handle.read()
        if text.startswith(f'To: {email}\n'):
            newest = text
    return newest


class PasswordRecoveryTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if not OUTBOX:
            raise unittest.SkipTest('EMAIL_OUTBOX_DIR is not set; the runner provides it.')
        cls.playwright = sync_playwright().start()
        cls.browser = cls.playwright.chromium.launch(headless=True, channel=os.environ.get('STORE_BROWSER_CHANNEL'))

    @classmethod
    def tearDownClass(cls):
        cls.browser.close()
        cls.playwright.stop()

    def new_page(self):
        context = self.browser.new_context(base_url=os.environ['STORE_BASE_URL'])
        self.addCleanup(context.close)
        return context.new_page()

    def register(self, page, email, password):
        page.goto('/account')
        form = page.locator('form[action="account"]').last
        form.locator('input[name="name"]').fill('Recovery Visitor')
        form.locator('input[name="email"]').fill(email)
        form.locator('input[name="password"]').fill(password)
        form.locator('#confirmationPassword').fill(password)
        form.locator('button[value="register"]').click()
        page.wait_for_load_state()

    def sign_in(self, page, email, password):
        page.goto('/account')
        form = page.locator('form[action="account"]').first
        form.locator('input[name="email"]').fill(email)
        form.locator('input[name="password"]').fill(password)
        form.locator('button[value="login"]').click()
        page.wait_for_load_state()

    def test_a_visitor_recovers_an_account_once_with_the_emailed_link(self):
        email = f'recovery-{uuid.uuid4().hex}@example.test'
        self.register(self.new_page(), email, 'original-password')

        page = self.new_page()
        page.goto('/account')
        page.get_by_role('link', name=re.compile('Forgot your password', re.I)).click()
        page.locator('#field-reset-email').fill(email)
        page.locator('button[type="submit"]').click()
        page.locator('.store-message').filter(has_text='If the account exists').wait_for()

        link = re.search(r'http\S+password-reset\?token=\S+', message_for(email) or '')
        self.assertIsNotNone(link, 'the demo outbox must hold a reset link for the account')
        page.goto(link.group(0))
        page.locator('#resetPassword').fill('short')
        page.locator('#resetPasswordConfirmation').fill('short')
        page.locator('button[type="submit"]').click()
        self.assertTrue(page.locator('#resetPassword').evaluate('(e) => !e.validity.valid'),
                        'the browser enforces the 8-character minimum before submitting')

        page.locator('#resetPassword').fill('a-brand-new-password')
        page.locator('#resetPasswordConfirmation').fill('a-brand-new-password')
        page.locator('button[type="submit"]').click()
        page.locator('.store-message').filter(has_text='Password updated').wait_for()

        self.sign_in(page, email, 'a-brand-new-password')
        page.locator('.store-message').filter(has_text='Welcome').wait_for()

        stranger = self.new_page()
        self.sign_in(stranger, email, 'original-password')
        stranger.locator('.store-message').filter(has_text='Invalid email or password').wait_for()

        reuse = self.new_page()
        reuse.goto(link.group(0))
        reuse.locator('#resetPassword').fill('yet-another-password')
        reuse.locator('#resetPasswordConfirmation').fill('yet-another-password')
        reuse.locator('button[type="submit"]').click()
        reuse.locator('.store-message').filter(has_text='invalid or has expired').wait_for()
        self.sign_in(reuse, email, 'a-brand-new-password')
        reuse.locator('.store-message').filter(has_text='Welcome').wait_for()

    def test_an_unknown_address_gets_the_same_answer_and_no_email(self):
        email = f'nobody-{uuid.uuid4().hex}@example.test'
        page = self.new_page()
        page.goto('/password-reset')
        page.locator('#field-reset-email').fill(email)
        page.locator('button[type="submit"]').click()

        page.locator('.store-message').filter(has_text='If the account exists').wait_for()
        self.assertIsNone(message_for(email))

    def test_forged_cross_site_reset_requests_are_refused(self):
        page = self.new_page()
        page.goto('/password-reset')
        response = page.request.post('password-reset', form={'action': 'request', 'email': 'a@example.test'})
        self.assertEqual(403, response.status)


if __name__ == '__main__':
    unittest.main()
