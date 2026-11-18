package br.uff.store.application.api;

import br.uff.store.core.interfaces.services.ICategoryService;
import br.uff.store.core.services.CategoryService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/categories/*")
public class CategoryApiServlet extends HttpServlet {
    private final ICategoryService categoriaService;

    public CategoryApiServlet() {
        this(new CategoryService());
    }

    CategoryApiServlet(ICategoryService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ApiResponses.writeJson(response, categoriaService.listaCategorys());
        } catch (Exception exception) {
            ApiResponses.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The request could not be completed.");
        }
    }
}
