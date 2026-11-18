package br.uff.store.application.controllers;

import br.uff.store.application.StoreServices;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.ICategoryService;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.core.services.CategoryService;
import br.uff.store.core.services.ProductService;
import br.uff.store.core.services.UserService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/product-admin")
public class ProductAdminServlet extends HttpServlet {
    private final IProductService productService;
    private final ICategoryService categoryService;
    private final IUserService userService;
    private final Helper helper;

    public ProductAdminServlet() {
        helper = new Helper();
        productService = StoreServices.products();
        categoryService = StoreServices.categories();
        userService = StoreServices.users();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {

        HttpSession session = request.getSession();
        try {

            Integer activeUserId = helper.tryParseInteger(String.valueOf(session.getAttribute("userId")));
            if (activeUserId != null && Boolean.TRUE.equals(userService.isAdmin(activeUserId))) {

                if (request.getParameter("selectedId") != null) {
                    String selParameterStr = request.getParameter("selectedId");
                    Integer selParameter = helper.tryParseInteger(selParameterStr);
                    ProductDTO product = new ProductDTO();
                    if(selParameter != null){
                        product = productService.findProductById(selParameter);
                    }

                    request.setAttribute("product", product);
                    request.setAttribute("categories", categoryService.categories());
                    session.setAttribute("selectedId", selParameterStr);
                    request.getRequestDispatcher("pages/product-edit.jsp").forward(request, response);
                    return;
                }

                Integer sel = null;
                if (session.getAttribute("selectedId") != null) {
                    sel = helper.tryParseInteger(session.getAttribute("selectedId").toString());
                }

                String action = "";
                if (request.getParameter("action") != null) {
                    action = request.getParameter("action");
                }
                if (("save".equals(action) || "addStock".equals(action) || "delete".equals(action))
                        && !"POST".equalsIgnoreCase(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }

                switch (action) {
                    case "save": {

                        ProductDTO productDto = new ProductDTO(
                            sel,
                            request.getParameter("name"),
                            Double.valueOf(request.getParameter("price")),
                            request.getParameter("description"),
                            request.getParameter("src"),
                            helper.tryParseInteger(request.getParameter("categoryId")),
                            0
                        );
                        
                        if (sel == null) {
                            productService.insertProduct(productDto);
                        } else {
                            productService.updateProductById(sel, productDto);
                        }
                        session.setAttribute("msg", "Product saved.");

                        break;
                    }
                    case "addStock": {
                        productService.addProductStock(sel, helper.tryParseInteger(request.getParameter("quantity")));
                        session.setAttribute("msg", "Stock increased.");
                        break;
                    }
                    case "delete": {
                        productService.deleteProductById(sel);
                        session.setAttribute("msg", "Product deleted.");
                        break;
                    }
                    case "clear": {

                        session.setAttribute("selectedId", null);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else {
                throw new StoreException("User is not authorized.");
            }

            request.setAttribute("grid", productService.listAdminProducts());

            request.getRequestDispatcher("pages/product-admin.jsp").forward(request, response);
        } catch (Exception e) {
            session.setAttribute("msg", e.getMessage());
            response.sendRedirect("account");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(ProductAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(ProductAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles storefront requests.";
    }// </editor-fold>

}
