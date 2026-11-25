package br.uff.store.application;

import javax.servlet.http.HttpServletRequest;

/**
 * Builds the address that goes into a password-reset email. A configured PUBLIC_BASE_URL is trusted; otherwise
 * the request's own address is used, which is only safe for local demos because a forged Host header could aim
 * the link at another site (listed in docs/non-production-readiness.md).
 */
public final class ResetLinks {
    private ResetLinks() { }

    public static String pageUrl(HttpServletRequest request, String configuredBaseUrl) {
        if (configuredBaseUrl != null && !configuredBaseUrl.trim().isEmpty()) {
            return configuredBaseUrl.trim().replaceAll("/+$", "") + "/password-reset";
        }
        int port = request.getServerPort();
        String portPart = port == 80 || port == 443 ? "" : ":" + port;
        return request.getScheme() + "://" + request.getServerName() + portPart + request.getContextPath() + "/password-reset";
    }
}
