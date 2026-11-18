package br.uff.store.application.api;

import br.uff.store.application.StoreServices;

import br.uff.store.core.interfaces.services.ICategoryService;
import br.uff.store.core.services.CategoryService;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/categories/*")
public class CategoryApiServlet extends HttpServlet {
    private final ICategoryService categoryService;

    public CategoryApiServlet() {
        this(StoreServices.categories());
    }

    CategoryApiServlet(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ApiResponses.writeJson(response, categoryService.categories());
        } catch (Exception exception) {
            ApiResponses.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The request could not be completed.");
        }
    }
}
