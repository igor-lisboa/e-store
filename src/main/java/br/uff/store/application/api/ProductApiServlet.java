package br.uff.store.application.api;

import br.uff.store.application.StoreServices;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.services.ProductService;
import br.uff.store.core.exceptions.ResourceNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/api/products/*")
public class ProductApiServlet extends HttpServlet {
    private final IProductService productService;

    public ProductApiServlet() {
        this(StoreServices.products());
    }

    ProductApiServlet(IProductService productService) {
        this.productService = productService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            try {
                List<ProductDTO> products = productService.listAdminProducts();
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
            ProductDTO product = productService.findProductById(productId);
            ApiResponses.writeJson(response, product);
        } catch (ResourceNotFoundException missing) {
            ApiResponses.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Product was not found.");
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
