package br.uff.store.application.controllers;

import br.uff.store.application.FlashMessages;
import br.uff.store.application.ResetLinks;
import br.uff.store.application.StoreServices;
import br.uff.store.core.interfaces.services.IPasswordResetService;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Account recovery pages: ask for a reset link, then choose a new password with the emailed token. */
@WebServlet("/password-reset")
public class PasswordResetServlet extends HttpServlet {
    private static final String VIEW = "pages/password-reset.jsp";
    private final IPasswordResetService resetService;
    private final String configuredBaseUrl;

    public PasswordResetServlet() {
        this(StoreServices.passwordResets(), System.getenv("PUBLIC_BASE_URL"));
    }

    PasswordResetServlet(IPasswordResetService resetService, String configuredBaseUrl) {
        this.resetService = resetService;
        this.configuredBaseUrl = configuredBaseUrl;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String token = request.getParameter("token");
        request.setAttribute("resetToken", token == null ? "" : token.trim());
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Locale locale = FlashMessages.localeOf(session);
        String action = request.getParameter("action");
        try {
            if ("request".equals(action)) {
                resetService.requestReset(request.getParameter("email"), ResetLinks.pageUrl(request, configuredBaseUrl));
                session.setAttribute("msg", FlashMessages.text("If the account exists, a reset link was sent to that email address.", locale));
                response.sendRedirect("account");
            } else if ("complete".equals(action)) {
                resetService.resetPassword(request.getParameter("token"), request.getParameter("password"));
                session.setAttribute("msg", FlashMessages.text("Password updated. Sign in with your new password.", locale));
                response.sendRedirect("account");
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception failure) {
            session.setAttribute("msg", FlashMessages.forFailure(failure, locale));
            String token = request.getParameter("token");
            response.sendRedirect("complete".equals(action) && token != null
                    ? "password-reset?token=" + URLEncoder.encode(token, "UTF-8") : "password-reset");
        }
    }
}
