"""Responsive layout checks across desktop, tablet, and small-phone viewports."""
from pathlib import Path
import os
import re
import unittest

from playwright.sync_api import sync_playwright


VIEWPORTS = {
    'desktop': {'width': 1440, 'height': 900},
    'tablet-portrait': {'width': 768, 'height': 1024},
    'tablet-landscape': {'width': 1024, 'height': 768},
    'small-mobile-portrait': {'width': 320, 'height': 568},
    'small-mobile-landscape': {'width': 568, 'height': 320},
}


class ResponsiveLayoutTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        if os.environ.get('STORE_DATABASE_E2E') != '1':
            raise RuntimeError('Responsive catalog checks require the disposable integration database.')
        cls.playwright = sync_playwright().start()
        cls.browser = cls.playwright.chromium.launch(
            headless=True, channel=os.environ.get('STORE_BROWSER_CHANNEL'))
        cls.screenshot_dir = Path('target/responsive-screenshots')
        cls.screenshot_dir.mkdir(parents=True, exist_ok=True)

    @classmethod
    def tearDownClass(cls):
        cls.browser.close()
        cls.playwright.stop()

    def open_admin_page(self, viewport):
        context = self.browser.new_context(base_url=os.environ['STORE_BASE_URL'], viewport=viewport)
        self.addCleanup(context.close)
        page = context.new_page()
        page.goto('/account')
        form = page.locator('form[action="account"]').first
        form.locator('[name="email"]').fill('admin@example.test')
        form.locator('[name="password"]').fill('development-password')
        form.locator('button[value="login"]').click()
        page.wait_for_load_state()
        return page

    def assert_page_fits_viewport(self, page, path, viewport_name):
        page.goto(path)
        page.wait_for_load_state()
        dimensions = page.evaluate("""() => ({
            viewport: window.innerWidth,
            document: document.documentElement.scrollWidth,
            body: document.body.scrollWidth
        })""")
        self.assertLessEqual(dimensions['document'], dimensions['viewport'] + 1,
                             f'{viewport_name} {path}: document overflows {dimensions}')
        self.assertLessEqual(dimensions['body'], dimensions['viewport'] + 1,
                             f'{viewport_name} {path}: body overflows {dimensions}')
        self.assertTrue(page.locator('header').is_visible(), f'{viewport_name} {path}: header hidden')
        self.assertTrue(page.locator('footer').is_visible(), f'{viewport_name} {path}: footer hidden')

    def test_primary_routes_fit_supported_viewports(self):
        routes = [
            '/', '/products', '/product?productId=1', '/account', '/addresses',
            '/product-admin', '/product-admin?selectedId=1', '/cart',
            '/checkout?history', '/reviews?productId=1', '/password-reset',
        ]
        pictured = {'/', '/products', '/product?productId=1', '/account'}
        for viewport_name, viewport in VIEWPORTS.items():
            with self.subTest(viewport=viewport_name):
                page = self.open_admin_page(viewport)
                for path in routes:
                    self.assert_page_fits_viewport(page, path, viewport_name)
                    if path in pictured:
                        route_name = re.sub(r'[^a-z0-9]+', '-', path.lower()).strip('-') or 'home'
                        page.screenshot(path=str(self.screenshot_dir / f'{viewport_name}-{route_name}.png'))


if __name__ == '__main__':
    unittest.main()
