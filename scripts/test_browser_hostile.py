"""Hostile browser cases: guests and other customers must not change someone else's reviews or orders."""
import os
import re
import unittest
import uuid

from playwright.sync_api import sync_playwright


def register(browser, label):
    """Create a fresh customer account in its own browser context and return (context, page)."""
    context = browser.new_context(base_url=os.environ['STORE_BASE_URL'])
    page = context.new_page()
    page.goto('/account')
    form = page.locator('form[action="account"]').last
    form.locator('input[name="name"]').fill(f'Hostile {label}')
    form.locator('input[name="email"]').fill(f'hostile-{label}-{uuid.uuid4().hex}@example.test')
    form.locator('input[name="password"]').fill('browser-test-password')
    form.locator('#confirmationPassword').fill('browser-test-password')
    form.locator('button[value="register"]').click()
    page.wait_for_load_state()
    return context, page


def csrf(page):
    return page.locator('meta[name="csrf-token"]').get_attribute('content')


def first_in_stock_product(page):
    page.goto('/products')
    link = page.locator('.products-item:not(:has(.products-unavailable)) a[href^="product?"]').first
    return int(re.search(r'productId=(\d+)', link.get_attribute('href')).group(1))


class HostileRequestsTest(unittest.TestCase):
    def setUp(self):
        self.playwright = sync_playwright().start()
        self.browser = self.playwright.chromium.launch(headless=True, channel=os.environ.get('STORE_BROWSER_CHANNEL'))
        self.contexts = []

    def tearDown(self):
        for context in self.contexts:
            context.close()
        self.browser.close()
        self.playwright.stop()

    def customer(self, label):
        context, page = register(self.browser, label)
        self.contexts.append(context)
        return context, page

    def test_another_customer_or_guest_cannot_change_or_delete_a_review(self):
        owner_context, owner = self.customer('owner')
        product_id = first_in_stock_product(owner)
        title = f'Owner review {uuid.uuid4().hex}'
        owner.goto(f'/reviews?productId={product_id}&rating=5')
        owner.locator('input[name="title"]').fill(title)
        owner.locator('textarea[name="description"]').fill('Only the owner may change this.')
        owner.locator('button[value="submitReview"]').click()
        owner.locator('.review-title').filter(has_text=title).first.wait_for()

        attacker_context, attacker = self.customer('attacker')
        attacker.goto(f'/product?productId={product_id}')
        token = csrf(attacker)
        for action in ('updateReview', 'deleteReview'):
            attacker_context.request.post('/reviews', form={
                'productId': str(product_id), 'action': action, 'rating': '1', 'title': 'Hijacked',
                'description': 'Hijacked', 'userId': '1', '_csrf': token})

        guest_context = self.browser.new_context(base_url=os.environ['STORE_BASE_URL'])
        self.contexts.append(guest_context)
        guest = guest_context.new_page()
        guest.goto('/products')
        guest_token = csrf(guest)
        for action in ('updateReview', 'deleteReview'):
            guest_context.request.post('/reviews', form={
                'productId': str(product_id), 'action': action, 'rating': '1', 'title': 'Hijacked',
                'description': 'Hijacked', '_csrf': guest_token})

        owner.goto(f'/product?productId={product_id}')
        self.assertEqual(1, owner.locator('.review-title').filter(has_text=title).count(),
                         'the owner review must survive hostile edits and deletes')
        self.assertEqual(0, owner.locator('.review-title').filter(has_text='Hijacked').count())

    def test_another_customer_or_guest_cannot_cancel_an_order(self):
        if os.environ.get('ALLOW_DEMO_CHECKOUT') != 'true':
            self.skipTest('Orders can only be placed when demo checkout is enabled.')
        owner_context, owner = self.customer('buyer')
        product_id = first_in_stock_product(owner)
        owner.goto(f'/product?productId={product_id}')
        owner.locator('button.cart-btn:not([disabled])').click()
        owner.goto('/addresses?selectedId=')
        for name, value in {'name': 'Hostile Home', 'zipcode': '12345', 'address': '1 Private Street',
                            'city': 'Test City', 'state': 'Test State'}.items():
            owner.locator(f'input[name="{name}"]').fill(value)
        owner.locator('button[value="save"]').click()
        owner.goto('/cart')
        owner.locator('button[value="confirmCart"]').click()
        owner.locator('input[name="addressId"]').first.check()
        owner.locator('button[value="continuePayment"]').click()
        owner.locator('button[value="paymentSuccess"]').click()
        owner.wait_for_url('**/checkout?history')
        order_id = int(re.search(r'#(\d+)', owner.locator('td.end-data').first.text_content()).group(1))

        attacker_context, attacker = self.customer('rival')
        attacker.goto('/cart')
        attacker_context.request.post('/checkout', form={
            'action': 'cancelOrder', 'orderId': str(order_id), '_csrf': csrf(attacker)})
        attacker.goto('/checkout?history')
        self.assertEqual(0, attacker.locator('.order-status').count(), 'a rival must not see or own that order')

        guest_context = self.browser.new_context(base_url=os.environ['STORE_BASE_URL'])
        self.contexts.append(guest_context)
        guest = guest_context.new_page()
        guest.goto('/products')
        guest_context.request.post('/checkout', form={
            'action': 'cancelOrder', 'orderId': str(order_id), '_csrf': csrf(guest)})

        owner_context.request.get(f'/checkout?action=cancelOrder&orderId={order_id}')
        owner.goto('/checkout?history')
        self.assertEqual('Placed', owner.locator('.order-status').first.text_content().strip(),
                         'the order must still be active after hostile and GET cancellations')

    def test_another_customer_or_guest_cannot_edit_or_delete_an_address(self):
        owner_context, owner = self.customer('home')
        owner.goto('/addresses?selectedId=')
        for name, value in {'name': 'Guarded Home', 'zipcode': '54321', 'address': '9 Guarded Street',
                            'city': 'Guard City', 'state': 'Guard State'}.items():
            owner.locator(f'input[name="{name}"]').fill(value)
        owner.locator('button[value="save"]').click()
        owner.goto('/addresses')
        href = owner.locator('a[href^="addresses?selectedId="]').filter(has_text='Edit').first.get_attribute('href')
        address_id = re.search(r'selectedId=(\d+)', href).group(1)

        attacker_context, attacker = self.customer('burglar')
        attacker.goto(f'/addresses?selectedId={address_id}')
        self.assertEqual(0, attacker.locator('input[value="9 Guarded Street"]').count(),
                         'another customer must not be shown the address form')
        token = csrf(attacker)
        attacker_context.request.post('/addresses', form={
            'action': 'save', 'selectedId': address_id, 'name': 'Taken', 'zipcode': '1', 'address': 'Stolen Street',
            'city': 'X', 'state': 'Y', '_csrf': token})
        attacker_context.request.post('/addresses', form={
            'action': 'delete', 'selectedId': address_id, '_csrf': token})

        guest_context = self.browser.new_context(base_url=os.environ['STORE_BASE_URL'])
        self.contexts.append(guest_context)
        guest = guest_context.new_page()
        guest.goto('/products')
        guest_context.request.post('/addresses', form={
            'action': 'delete', 'selectedId': address_id, '_csrf': csrf(guest)})

        owner.goto(f'/addresses?selectedId={address_id}')
        self.assertEqual('9 Guarded Street', owner.locator('input[name="address"]').input_value(),
                         'the address must be unchanged after hostile edits and deletes')


if __name__ == '__main__':
    unittest.main()
