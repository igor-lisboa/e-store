package br.uff.store.application.security;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HtmlTest {
    @Test
    public void markupAndAttributeBreakoutsBecomeLiteralText() {
        assertEquals("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;&amp;&quot;",
                Html.escape("<script>alert('x')</script>&\""));
    }

    @Test
    public void nullAndOrdinaryTextRenderWithoutIntroducingMarkup() {
        assertEquals("", Html.escape(null));
        assertEquals("Console 2", Html.escape("Console 2"));
    }
}
