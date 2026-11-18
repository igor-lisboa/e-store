package br.uff.loja.application.api;

import br.uff.loja.core.interfaces.services.ICategoriaService;
import br.uff.loja.core.services.CategoriaService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/categoria/*")
public class CategoriaApiServlet extends HttpServlet {
    private final ICategoriaService categoriaService;

    public CategoriaApiServlet() {
        this(new CategoriaService());
    }

    CategoriaApiServlet(ICategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ApiResponses.writeJson(response, categoriaService.listaCategorias());
        } catch (Exception exception) {
            ApiResponses.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The request could not be completed.");
        }
    }
}
