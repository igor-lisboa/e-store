package br.uff.store.application.controllers;

import br.uff.store.application.FlashMessages;
import br.uff.store.application.StoreServices;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.uff.store.core.interfaces.services.IReviewService;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.services.ReviewService;
import br.uff.store.core.services.ProductService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/product")
public class ProductServlet extends HttpServlet {
    private static final String PRODUCT_ID = "productId";
    private final IProductService productService;
    private final IReviewService reviewService;
    private final Helper helper;

    public ProductServlet() {
        this(StoreServices.products(), StoreServices.reviews(), new Helper());
    }

    ProductServlet(IProductService productService, IReviewService reviewService, Helper helper) {
        this.productService = productService;
        this.reviewService = reviewService;
        this.helper = helper;
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        try {

            session.setAttribute("rating", null);

            Integer productId = null;
            if (request.getParameter(PRODUCT_ID) != null) {
                productId = helper.tryParseInteger(request.getParameter(PRODUCT_ID));
                session.setAttribute(PRODUCT_ID, productId);
            }

            if (session.getAttribute(PRODUCT_ID) != null) {
                productId = helper.tryParseInteger(session.getAttribute(PRODUCT_ID).toString());
            } else {
                session.setAttribute("msg", FlashMessages.text("Please select a product.", FlashMessages.localeOf(session)));
                response.sendRedirect("products");
                return;
            }

            Integer userId = null;
            if (session.getAttribute("userId") != null) {
                userId = helper.tryParseInteger(session.getAttribute("userId").toString());
            }            

            if (request.getParameter("fav") != null) {
                if (!"POST".equalsIgnoreCase(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }

                if (userId == null) {
                    session.setAttribute("msg", FlashMessages.text("Sign in to save a favorite.", FlashMessages.localeOf(session)));
                    response.sendRedirect("account?redirect=product");
                    return;
                } else {
                    productService.toggleFavorite(productId, userId);
                    request.removeAttribute("fav");
                    response.sendRedirect("product");
                    return;
                }
            }
            request.setAttribute("product", productService.getCustomerProduct(productId, userId));
            request.setAttribute("reviews", reviewService.listProductReviews(productId));

            request.getRequestDispatcher("pages/product.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", FlashMessages.forFailure(ex, FlashMessages.localeOf(session)));
            response.sendRedirect("products");
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
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
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
