package br.uff.store.application.api;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.core.services.UserService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/users/*")
public class UserApiServlet extends HttpServlet {
    private final IUserService usuarioService;

    public UserApiServlet() {
        this(new UserService());
    }

    UserApiServlet(IUserService usuarioService) {
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
                ApiResponses.writeJson(response, usuarioService.listaUsers());
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
            ApiResponses.writeJson(response, usuarioService.encontraUserPorId(requestedUserId));
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
