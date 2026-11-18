package br.uff.loja.application.controllers;

import java.io.IOException;
import br.uff.loja.application.i18n.LocalePreferences;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/language")
public class LanguageServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestedLocale = request.getParameter("locale");
        if (LocalePreferences.supports(requestedLocale)) {
            request.getSession(true).setAttribute("appLocale", LocalePreferences.resolve(requestedLocale));
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}
