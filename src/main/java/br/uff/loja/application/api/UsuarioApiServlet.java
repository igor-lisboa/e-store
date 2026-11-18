package br.uff.loja.application.api;

import br.uff.loja.core.dtos.UsuarioDTO;
import br.uff.loja.core.interfaces.services.IUsuarioService;
import br.uff.loja.core.services.UsuarioService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/usuario/*")
public class UsuarioApiServlet extends HttpServlet {
    private final IUsuarioService usuarioService;

    public UsuarioApiServlet() {
        this(new UsuarioService());
    }

    UsuarioApiServlet(IUsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        Integer activeUserId = sessionInteger(session, "userId");
        if (activeUserId == null) {
            ApiResponses.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required.");
            return;
        }

        try {
            boolean administrator = usuarioService.ehAdmin(activeUserId);
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
                if (!administrator) {
                    ApiResponses.writeError(response, HttpServletResponse.SC_FORBIDDEN, "Administrator access is required.");
                    return;
                }
                ApiResponses.writeJson(response, usuarioService.listaUsuarios());
                return;
            }

            Integer requestedUserId = parseUserId(pathInfo);
            if (requestedUserId == null) {
                ApiResponses.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "A valid user ID is required.");
                return;
            }
            if (!administrator && !activeUserId.equals(requestedUserId)) {
                ApiResponses.writeError(response, HttpServletResponse.SC_FORBIDDEN, "You cannot view this account.");
                return;
            }
            ApiResponses.writeJson(response, usuarioService.encontraUsuarioPorId(requestedUserId));
        } catch (Exception exception) {
            ApiResponses.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The request could not be completed.");
        }
    }

    private Integer sessionInteger(HttpSession session, String attribute) {
        if (session == null || session.getAttribute(attribute) == null) {
            return null;
        }
        try {
            return Integer.valueOf(session.getAttribute(attribute).toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseUserId(String pathInfo) {
        String value = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (value.isEmpty() || value.contains("/")) {
            return null;
        }
        try {
            int userId = Integer.parseInt(value);
            return userId > 0 ? userId : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

}
