package br.uff.loja.application.api;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

final class ApiResponses {
    private static final Gson GSON = new Gson();

    private ApiResponses() { }

    static void writeJson(HttpServletResponse response, Object body) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(GSON.toJson(body));
    }

    static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        writeJson(response, new ErrorResponse(message));
    }

    private static final class ErrorResponse {
        private final String error;

        private ErrorResponse(String error) {
            this.error = error;
        }
    }
}
