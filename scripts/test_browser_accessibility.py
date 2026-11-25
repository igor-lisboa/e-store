"""Browser accessibility checks: keyboard focus is visible, tab order follows the page, and text is readable."""
import os
import re
import unittest

from playwright.sync_api import sync_playwright

# Properties that can make a focused control look different from an unfocused one.
FOCUS_STYLE_SCRIPT = """
(element) => {
    const style = getComputedStyle(element);
    return [style.outlineStyle, style.outlineWidth, style.outlineColor, style.boxShadow, style.borderTopColor,
            style.borderBottomColor, style.backgroundColor, style.color, style.textDecorationLine].join('|');
}
"""

# Contrast ratio per WCAG 2.1 for text whose background is a solid colour.
CONTRAST_SCRIPT = """
() => {
    const parse = (value) => {
        const m = value.match(/rgba?\\(([^)]+)\\)/);
        if (!m) return null;
        const p = m[1].split(',').map(Number);
        return {r: p[0], g: p[1], b: p[2], a: p.length > 3 ? p[3] : 1};
    };
    const channel = (c) => { c /= 255; return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4); };
    const luminance = (c) => 0.2126 * channel(c.r) + 0.7152 * channel(c.g) + 0.0722 * channel(c.b);
    const solidBackground = (element) => {
        for (let node = element; node; node = node.parentElement) {
            const style = getComputedStyle(node);
            if (style.backgroundImage !== 'none') return null;  // gradients and photos cannot be judged here
            const colour = parse(style.backgroundColor);
            if (colour && colour.a === 1) return colour;
            if (colour && colour.a > 0) return null;
        }
        return {r: 255, g: 255, b: 255, a: 1};
    };
    const failures = [];
    const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
    const seen = new Set();
    while (walker.nextNode()) {
        const node = walker.currentNode;
        const element = node.parentElement;
        if (!node.textContent.trim() || seen.has(element)) continue;
        if (['SCRIPT', 'STYLE', 'NOSCRIPT'].includes(element.tagName)) continue;
        const style = getComputedStyle(element);
        const box = element.getBoundingClientRect();
        if (style.visibility === 'hidden' || style.display === 'none' || box.width === 0 || box.height === 0) continue;
        if (element.closest('[disabled], [aria-disabled="true"], .products-unavailable')) continue;
        seen.add(element);
        const foreground = parse(style.color);
        const background = solidBackground(element);
        if (!foreground || !background) continue;
        const lighter = Math.max(luminance(foreground), luminance(background));
        const darker = Math.min(luminance(foreground), luminance(background));
        const ratio = (lighter + 0.05) / (darker + 0.05);
        const size = parseFloat(style.fontSize);
        const large = size >= 24 || (size >= 18.66 && parseInt(style.fontWeight, 10) >= 700);
        if (ratio < (large ? 3 : 4.5)) {
            failures.push(`${element.tagName.toLowerCase()}.${element.className} "${node.textContent.trim().slice(0, 30)}" ratio ${ratio.toFixed(2)}`);
        }
    }
    return failures;
}
"""


class AccessibilityTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.playwright = sync_playwright().start()
        cls.browser = cls.playwright.chromium.launch(headless=True, channel=os.environ.get('STORE_BROWSER_CHANNEL'))

    @classmethod
    def tearDownClass(cls):
        cls.browser.close()
        cls.playwright.stop()

    def open(self, path):
        context = self.browser.new_context(base_url=os.environ['STORE_BASE_URL'], viewport={'width': 1280, 'height': 900})
        self.addCleanup(context.close)
        page = context.new_page()
        page.goto(path)
        page.wait_for_load_state()
        return page

    def first_product_path(self):
        page = self.open('/products')
        href = page.locator('a[href^="product?"]').first.get_attribute('href')
        return '/' + href

    def pages(self):
        return ['/account', '/password-reset', '/password-reset?token=example', '/products', self.first_product_path(), '/']

    def test_no_page_forces_a_custom_tab_order(self):
        for path in self.pages():
            page = self.open(path)
            self.assertEqual(0, page.locator('[tabindex]:not([tabindex="0"]):not([tabindex="-1"])').count(), path)

    def test_every_keyboard_stop_shows_a_visible_focus_change(self):
        for path in self.pages():
            page = self.open(path)
            invisible = []
            for _ in range(80):
                page.keyboard.press('Tab')
                focused = page.evaluate_handle('document.activeElement')
                element = focused.as_element()
                if element is None or page.evaluate('(e) => e === document.body', element):
                    break
                focused_style = page.evaluate(FOCUS_STYLE_SCRIPT, element)
                description = page.evaluate('(e) => e.tagName.toLowerCase() + (e.name ? "[" + e.name + "]" : "") + "." + e.className', element)
                if not page.evaluate('(e) => e.matches(":focus-visible")', element):
                    invisible.append(f'{description} did not match :focus-visible')
                    continue
                page.evaluate('(e) => e.blur()', element)
                blurred_style = page.evaluate(FOCUS_STYLE_SCRIPT, element)
                if focused_style == blurred_style:
                    invisible.append(f'{description} looks identical when focused')
                element.focus()
            self.assertEqual([], invisible, f'{path}: focus is not visible for these controls')

    def test_text_meets_the_minimum_contrast_on_solid_backgrounds(self):
        for path in self.pages():
            page = self.open(path)
            failures = page.evaluate(CONTRAST_SCRIPT)
            self.assertEqual([], failures, f'{path}: low-contrast text')

    def test_every_form_control_is_reachable_and_named_for_assistive_technology(self):
        for path in self.pages():
            page = self.open(path)
            unnamed = page.evaluate("""() => Array.from(document.querySelectorAll('input:not([type=hidden]), select, textarea'))
                .filter(e => getComputedStyle(e).display !== 'none' && e.style.display !== 'none')
                .filter(e => !(e.labels && e.labels.length) && !e.getAttribute('aria-label') && !e.getAttribute('aria-labelledby'))
                .map(e => e.tagName + '[' + (e.name || e.id) + ']')""")
            self.assertEqual([], unnamed, path)

    def test_invalid_input_is_announced_by_the_browser_before_submission(self):
        page = self.open('/account')
        form = page.locator('form[action="account"]').last
        form.locator('input[name="email"]').fill('not-an-email')
        form.locator('input[name="password"]').fill('short')
        form.locator('button[value="register"]').click()
        self.assertTrue(re.search(r'account', page.url))
        invalid = page.evaluate("""() => Array.from(document.querySelectorAll(':invalid')).map(e => e.name || e.id)""")
        self.assertIn('email', invalid)
        self.assertIn('password', invalid)
        message = form.locator('input[name="email"]').evaluate('(e) => e.validationMessage')
        self.assertTrue(message, 'the browser provides a validation message that screen readers announce')


if __name__ == '__main__':
    unittest.main()
