package br.uff.store.application.api;

import br.uff.store.application.StoreServices;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.core.exceptions.ResourceNotFoundException;
import br.uff.store.infrastructure.shared.Helper;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/products/*")
public class ProductApiServlet extends HttpServlet {
    private final IProductService productService;
    private final IUserService userService;
    private final Helper helper;

    public ProductApiServlet() {
        this(StoreServices.products(), StoreServices.users(), new Helper());
    }

    ProductApiServlet(IProductService productService, IUserService userService, Helper helper) {
        this.productService = productService;
        this.userService = userService;
        this.helper = helper;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            listAllProducts(request, response);
            return;
        }

        Integer productId = parseProductId(pathInfo);
        if (productId == null) {
            ApiResponses.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "A valid product ID is required.");
            return;
        }
        try {
            ProductDTO product = productService.findProductById(productId);
            if (isAdministrator(request)) {
                ApiResponses.writeJson(response, product);
            } else {
                ApiResponses.writeJson(response, new PublicProductView(product));
            }
        } catch (ResourceNotFoundException missing) {
            ApiResponses.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Product was not found.");
        } catch (Exception exception) {
            ApiResponses.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The request could not be completed.");
        }
    }

    private boolean isAdministrator(HttpServletRequest request) throws Exception {
        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : helper.tryParseInteger(String.valueOf(session.getAttribute("userId")));
        return userId != null && Boolean.TRUE.equals(userService.isAdmin(userId));
    }

    private void listAllProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Integer activeUserId = session == null ? null : helper.tryParseInteger(String.valueOf(session.getAttribute("userId")));
        if (activeUserId == null) {
            ApiResponses.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication is required.");
            return;
        }
        try {
            if (!Boolean.TRUE.equals(userService.isAdmin(activeUserId))) {
                ApiResponses.writeError(response, HttpServletResponse.SC_FORBIDDEN, "Administrator access is required.");
                return;
            }
            ApiResponses.writeJson(response, productService.listAdminProducts());
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
