package br.uff.store.application.controllers;

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

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.interfaces.services.IReviewService;
import br.uff.store.core.services.ReviewService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/reviews")
public class ReviewServlet extends HttpServlet {
    private final IReviewService reviewService;
    private static final String PRODUCT_ID = "productId";
    private static final String RATING = "rating";
    private final Helper helper;

    public ReviewServlet() {
        reviewService = StoreServices.reviews();
        helper = new Helper();
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

            if (request.getParameter(PRODUCT_ID) != null) {
                session.setAttribute(PRODUCT_ID, request.getParameter(PRODUCT_ID));
            }

            if (request.getParameter(RATING) != null) {
                session.setAttribute(RATING, request.getParameter(RATING));
            }

            Integer userId = null;
            if (session.getAttribute("userId") == null) {
                session.setAttribute("msg", "Sign in to review a product.");
                response.sendRedirect("account?redirect=reviews");
                return;
            } else {
                userId = helper.tryParseInteger(session.getAttribute("userId").toString());
            }

            Integer productId = null;
            if (session.getAttribute(PRODUCT_ID) != null) {
                productId = helper.tryParseInteger(session.getAttribute(PRODUCT_ID).toString());
            } else {
                session.setAttribute("msg", "Please select a product.");
                response.sendRedirect("products");
                return;
            }

            String action = "";
            if (request.getParameter("action") != null) {
                action = request.getParameter("action");
            }

            if (action.equals("submitReview")) {
                if (!"POST".equalsIgnoreCase(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                Integer rating = helper.tryParseInteger(request.getParameter(RATING));
                reviewService.reviewProduct(new ProductReviewInputDTO(
                        userId, productId, rating, request.getParameter("description"), request.getParameter("title")));
                session.setAttribute(RATING, null);
                response.sendRedirect("product");
                return;
            }

            request.getRequestDispatcher("pages/product-review.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", ex.getMessage());
            response.sendRedirect("product");
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
            Logger.getLogger(ReviewServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ReviewServlet.class.getName()).log(Level.SEVERE, null, ex);
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

