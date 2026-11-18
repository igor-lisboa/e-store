package br.uff.loja.application.api;

import br.uff.loja.core.dtos.ProdutoDTO;
import br.uff.loja.core.interfaces.services.IProdutoService;
import br.uff.loja.core.services.ProdutoService;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/produto/*")
public class ProdutoApiServlet extends HttpServlet {
    private final IProdutoService produtoService;

    public ProdutoApiServlet() {
        this(new ProdutoService());
    }

    ProdutoApiServlet(IProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            try {
                List<ProdutoDTO> products = produtoService.listaProdutosAdm();
                ApiResponses.writeJson(response, products);
            } catch (Exception exception) {
                ApiResponses.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "The request could not be completed.");
            }
            return;
        }

        Integer productId = parseProductId(pathInfo);
        if (productId == null) {
            ApiResponses.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "A valid product ID is required.");
            return;
        }
        try {
            ProdutoDTO product = produtoService.encontraProdutoPorId(productId);
            ApiResponses.writeJson(response, product);
        } catch (Exception exception) {
            ApiResponses.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The request could not be completed.");
        }
    }

    private Integer parseProductId(String pathInfo) {
        String value = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (value.isEmpty() || value.contains("/")) {
            return null;
        }
        try {
            int productId = Integer.parseInt(value);
            return productId > 0 ? productId : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
