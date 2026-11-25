"""Shopping journey against an explicitly disposable, migrated integration database."""
import os
import base64
import re
import unittest
import uuid
from urllib.parse import quote

from playwright.sync_api import sync_playwright


def read_stock_as_administrator(browser, product_id):
    """Exact stock is administrator-only, so customer journeys read it through a separate admin session."""
    admin = browser.new_context(base_url=os.environ['STORE_BASE_URL'])
    try:
        admin_page = admin.new_page()
        admin_page.goto('/account')
        login = admin_page.locator('form[action="account"]').first
        login.locator('[name="email"]').fill('admin@example.test')
        login.locator('[name="password"]').fill('development-password')
        login.locator('button[value="login"]').click()
        return admin.request.get(f'/api/products/{product_id}').json()['quantity']
    finally:
        admin.close()


class StoreShoppingJourneyTest(unittest.TestCase):
    def test_administrator_can_manage_catalog_and_customer_cannot_change_stock(self):
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch(headless=True, channel=os.environ.get('STORE_BROWSER_CHANNEL'))
            context = browser.new_context(base_url=os.environ['STORE_BASE_URL'])
            page = context.new_page()
            try:
                page.goto('/account')
                login = page.locator('form[action="account"]').first
                login.locator('[name="email"]').fill('admin@example.test')
                login.locator('[name="password"]').fill('development-password')
                login.locator('button[value="login"]').click()
                accounts = context.request.get('/api/users/')
                self.assertEqual(200, accounts.status)
                for account in accounts.json():
                    self.assertTrue({'id', 'name', 'email', 'roleId'}.issubset(account))
                    self.assertNotIn('password', account)
                categories = context.request.get('/api/categories/')
                self.assertEqual(200, categories.status)
                category_names = {category['name'] for category in categories.json()}
                self.assertIn('Xbox > Games', category_names)
                self.assertIn('Nintendo > Games', category_names)
                for path, status in {'/api/products/0': 400, '/api/products/invalid': 400,
                                     '/api/products/2147483647': 404, '/api/users/0': 400,
                                     '/api/users/2147483647': 404}.items():
                    response = context.request.get(path)
                    self.assertEqual(status, response.status, path)
                    self.assertEqual({'error'}, set(response.json()))
                page.goto('/product-admin?selectedId=')
                name = 'Browser Catalog ' + uuid.uuid4().hex
                page.locator('[name="name"]').fill(name)
                page.locator('[name="price"]').fill('12.50')
                page.locator('[name="description"]').fill('Administrator-created product.')
                page.locator('[name="categoryId"]').select_option('5')
                image = base64.b64decode('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jR1sAAAAASUVORK5CYII=')
                page.locator('[name="img"]').set_input_files({'name': 'pixel.png', 'mimeType': 'image/png', 'buffer': image})
                page.wait_for_function('document.querySelector("[name=src]").value.startsWith("data:image/")')
                page.locator('button[value="save"]').click()
                row = page.locator('tr.product-data').filter(has_text=name)
                href = row.locator('a').get_attribute('href')
                product_id = int(re.search(r'selectedId=(\d+)', href).group(1))
                page.goto('/products?search=' + quote(name))
                listed = page.locator('.products-item').filter(has_text=name)
                self.assertEqual(1, listed.count(), 'out-of-stock products stay listed')
                self.assertEqual(1, listed.locator('.products-unavailable').count())
                listed.locator('a').click()
                self.assertEqual(0, page.locator('button.cart-btn:not([disabled])').count())
                self.assertEqual(1, page.locator('button.cart-btn[disabled]').count())
                page.goto(f'/product-admin?selectedId={product_id}')
                page.locator('[name="quantity"]').fill('5')
                page.locator('button[value="addStock"]').click()
                page.goto('/products?search=' + quote(name))
                self.assertEqual(0, page.locator('.products-item').filter(has_text=name)
                                 .locator('.products-unavailable').count())
                page.goto(f'/product?productId={product_id}')
                self.assertEqual(1, page.locator('button.cart-btn:not([disabled])').count())
                page.goto(f'/product-admin?selectedId={product_id}')
                self.assertEqual(5, context.request.get(f'/api/products/{product_id}').json()['quantity'])
                page.goto(f'/product-admin?selectedId={product_id}')
                page.locator('[name="price"]').fill('15.75')
                page.locator('button[value="save"]').click()
                self.assertEqual(15.75, context.request.get(f'/api/products/{product_id}').json()['price'])

                customer = browser.new_context(base_url=os.environ['STORE_BASE_URL'])
                try:
                    customer_page = customer.new_page()
                    customer_page.goto('/account')
                    form = customer_page.locator('form[action="account"]').first
                    form.locator('[name="email"]').fill('customer@example.test')
                    form.locator('[name="password"]').fill('development-password')
                    form.locator('button[value="login"]').click()
                    own_account = customer.request.get('/api/users/2')
                    self.assertEqual(200, own_account.status)
                    self.assertEqual('customer@example.test', own_account.json()['email'])
                    self.assertNotIn('password', own_account.json())
                    self.assertEqual(403, customer.request.get('/api/users/1').status)
                    self.assertEqual(403, customer.request.get('/api/users/').status)
                    token = customer_page.locator('meta[name="csrf-token"]').get_attribute('content')
                    customer.request.get(f'/product-admin?selectedId={product_id}')
                    customer.request.post('/product-admin', form={'action': 'addStock', 'quantity': '100', '_csrf': token})
                    self.assertEqual(5, context.request.get(f'/api/products/{product_id}').json()['quantity'])
                finally:
                    customer.close()
                page.goto(f'/product-admin?selectedId={product_id}')
                page.once('dialog', lambda dialog: dialog.accept())
                page.locator('button[value="delete"]').click()
                self.assertEqual(404, context.request.get(f'/api/products/{product_id}').status)
            finally:
                context.close()
                browser.close()

    def test_customer_can_register_review_favorite_and_checkout_without_overselling(self):
        if os.environ.get('STORE_DATABASE_E2E') != '1':
            self.fail('Run via scripts/run_browser_smoke.py with STORE_DATABASE_E2E=1.')
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch(headless=True, channel=os.environ.get('STORE_BROWSER_CHANNEL'))
            context = browser.new_context(base_url=os.environ['STORE_BASE_URL'])
            page = context.new_page()
            try:
                unique = uuid.uuid4().hex
                page.goto('/account')
                register = page.locator('form[action="account"]').last
                register.locator('input[name="name"]').fill('Browser Customer')
                register.locator('input[name="email"]').fill(f'browser-{unique}@example.test')
                register.locator('input[name="password"]').fill('browser-test-password')
                register.locator('#confirmationPassword').fill('browser-test-password')
                register.locator('button[value="register"]').click()
                page.wait_for_load_state()
                self.assertEqual(1, page.locator('button[value="logout"]').count())

                page.goto('/products')
                product_link = page.locator('.products-item a[href^="product?"]').first
                href = product_link.get_attribute('href')
                self.assertIsNotNone(href)
                product_id = int(re.search(r'productId=(\d+)', href).group(1))
                before = read_stock_as_administrator(browser, product_id)
                public_product = context.request.get(f'/api/products/{product_id}').json()
                self.assertNotIn('quantity', public_product)
                self.assertTrue(public_product['available'])
                product_link.click()
                with page.expect_navigation():
                    page.locator('label[for="toggle-heart"]').click()
                self.assertTrue(page.locator('#toggle-heart').is_checked())

                page.goto(f'/reviews?productId={product_id}&rating=4')
                review_title = f'<strong>Review {unique}</strong>'
                page.locator('input[name="title"]').fill(review_title)
                page.locator('textarea[name="description"]').fill('Browser journey review.')
                page.locator('button[value="submitReview"]').click()
                review = page.locator('.review-title').filter(has_text=review_title).first
                review.wait_for()
                self.assertIn(review_title, review.text_content())
                self.assertEqual(0, page.locator('.review-title strong').count())

                page.goto(f'/product?productId={product_id}')
                self.assertEqual(0, page.locator('.review-btn a[href$="reviews?productId=%d"]' % product_id).count())
                page.locator('.review-btn a[href*="&edit=1"]').click()
                self.assertEqual(review_title, page.locator('input[name="title"]').input_value())
                edited_title = f'Edited {unique}'
                page.locator('input[name="title"]').fill(edited_title)
                page.locator('button[value="updateReview"]').click()
                page.locator('.review-title').filter(has_text=edited_title).first.wait_for()
                self.assertEqual(0, page.locator('.review-title').filter(has_text=review_title).count())
                page.locator('.review-btn a[href*="&edit=1"]').click()
                page.once('dialog', lambda dialog: dialog.accept())
                page.locator('button[value="deleteReview"]').click()
                page.wait_for_url('**/product')
                self.assertEqual(0, page.locator('.review-title').filter(has_text=edited_title).count())
                self.assertEqual(1, page.locator('.review-btn a[href^="reviews?productId="]').count())

                page.locator('button.cart-btn').click()
                quantity = page.locator('input[name="quantity"]').first
                quantity.fill('2')
                with page.expect_navigation():
                    quantity.press('Tab')
                self.assertEqual('2', page.locator('input[name="quantity"]').first.input_value())

                page.goto('/addresses?selectedId=')
                for name, value in {'name': 'Browser Home', 'zipcode': '12345', 'address': '12 Main Street',
                                    'city': 'Test City', 'state': 'Test State'}.items():
                    page.locator(f'input[name="{name}"]').fill(value)
                page.locator('button[value="save"]').click()
                page.goto('/cart')
                page.locator('button[value="confirmCart"]').click()
                page.locator('input[name="addressId"]').first.check()
                page.locator('button[value="continuePayment"]').click()

                if os.environ.get('ALLOW_DEMO_CHECKOUT') == 'true':
                    page.locator('button[value="paymentSuccess"]').click()
                    page.wait_for_url('**/checkout?history')
                    self.assertTrue(page.get_by_text('12 Main Street', exact=False).first.is_visible())
                    after = read_stock_as_administrator(browser, product_id)
                    self.assertEqual(before - 2, after)
                    self.assertEqual('Placed', page.locator('.order-status').first.text_content().strip())
                    page.once('dialog', lambda dialog: dialog.accept())
                    with page.expect_navigation():
                        page.locator('button[value="cancelOrder"]').first.click()
                    self.assertEqual('Cancelled', page.locator('.order-status').first.text_content().strip())
                    self.assertEqual(0, page.locator('button[value="cancelOrder"]').count())
                    self.assertEqual(before, read_stock_as_administrator(browser, product_id))
                else:
                    self.assertEqual(0, page.locator('button[value="paymentSuccess"]').count())
                    token = page.locator('meta[name="csrf-token"]').get_attribute('content')
                    response = context.request.post('/checkout', form={'action': 'paymentSuccess', '_csrf': token})
                    self.assertIn('Demo checkout is disabled', response.text())
                    after = read_stock_as_administrator(browser, product_id)
                    self.assertEqual(before, after)

                page.goto('/account')
                old_token = page.locator('input[name="_csrf"]').first.input_value()
                page.once('dialog', lambda dialog: dialog.accept())
                page.locator('button[value="logout"]').click()
                expired = context.request.post('/language', form={'locale': 'es', '_csrf': old_token})
                self.assertEqual(403, expired.status)
                page.goto('/account')
                login = page.locator('form[action="account"]').first
                login.locator('input[name="email"]').fill(f'browser-{unique}@example.test')
                login.locator('input[name="password"]').fill('browser-test-password')
                login.locator('input[name="remember"]').check()
                login.locator('button[value="login"]').click()
                self.assertEqual(1, page.locator('button[value="logout"]').count())
                self.assertNotIn('loginPassword', {cookie['name'] for cookie in context.cookies()})
                self.assertEqual(403, context.request.get('/api/users/').status)
            finally:
                context.close()
                browser.close()


    def test_anonymous_cart_is_private_and_merges_into_the_account_cart_on_sign_in(self):
        if os.environ.get('STORE_DATABASE_E2E') != '1':
            self.fail('Run via scripts/run_browser_smoke.py with STORE_DATABASE_E2E=1.')
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch(headless=True, channel=os.environ.get('STORE_BROWSER_CHANNEL'))
            visitor = browser.new_context(base_url=os.environ['STORE_BASE_URL'])
            stranger = browser.new_context(base_url=os.environ['STORE_BASE_URL'])
            try:
                page = visitor.new_page()
                page.goto('/products')
                page.locator('.products-item a[href^="product?"]').first.click()
                product_name = page.locator('.product-description h1').text_content().strip()
                page.locator('button.cart-btn:not([disabled])').click()
                page.goto('/cart')
                self.assertEqual(1, page.locator('.cart-name').filter(has_text=product_name).count(),
                                 'an anonymous visitor keeps the cart within the session')

                other = stranger.new_page()
                other.goto('/cart')
                self.assertEqual(0, other.locator('.cart-name').count(), 'another visitor must not see the cart')

                page.goto('/account')
                login = page.locator('form[action="account"]').first
                login.locator('[name="email"]').fill('customer@example.test')
                login.locator('[name="password"]').fill('development-password')
                login.locator('button[value="login"]').click()
                page.goto('/cart')
                self.assertEqual(1, page.locator('.cart-name').filter(has_text=product_name).count(),
                                 'signing in merges the anonymous cart into the account cart')
            finally:
                visitor.close()
                stranger.close()
                browser.close()


if __name__ == '__main__':
    unittest.main()
