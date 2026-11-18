package br.uff.store.application.security;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/** Validate before a controller can authenticate a user or change session/data state. */
@WebFilter(urlPatterns = {"/", "/account", "/addresses", "/product-admin", "/product",
        "/products", "/cart", "/checkout", "/reviews", "/language", "/pages/*"})
public class CsrfFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) { }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        request.setCharacterEncoding("UTF-8");
        String method = request.getMethod();
        boolean safeMethod = "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
        if (!safeMethod && !CsrfTokens.matches(request.getSession(false), request.getParameter(CsrfTokens.PARAMETER))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "The form expired or could not be verified. Reload the page and try again.");
            return;
        }
        if (!"OPTIONS".equals(method)) {
            CsrfTokens.getOrCreate(request.getSession(true));
            response.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
