package br.uff.store.application.controllers;

import br.uff.store.application.StoreServices;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.ISaleService;
import br.uff.store.core.services.CartService;
import br.uff.store.core.services.AddressService;
import br.uff.store.core.services.SaleService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private final ISaleService saleService;
    private final ICartService cartService;
    private final IAddressService addressService;
    private final Helper helper;

    public CheckoutServlet() {
        saleService = StoreServices.sales();
        cartService = StoreServices.carts();
        addressService = StoreServices.addresses();
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

            Integer userId = null;
            if (session.getAttribute("userId") == null) {
                response.sendRedirect("account?redirect=cart");
                return;
            } else {
                userId = helper.tryParseInteger(session.getAttribute("userId").toString());
            }

            if (request.getParameter("history") != null) {
                Integer pageNumber = 1;
                if (request.getParameter("page") != null) {
                    pageNumber = helper.tryParseInteger(request.getParameter("page"));
                }

                PaginationDTO<List<SaleDTO>> sales = saleService.listUserSales(userId, 5, pageNumber);
                for(SaleDTO sale : sales.getItems()) {
                    sale.setCartProducts(cartService.listCartProducts(sale.getCartId()));
                    sale.setAddress(addressService.findAddressById(sale.getAddressId()));
                }
                request.setAttribute("sales", sales);

                request.getRequestDispatcher("pages/orders.jsp").forward(request, response);
                return;
            }

            Integer addressId = null;
            if (session.getAttribute("addressId") != null) {
                addressId = helper.tryParseInteger(session.getAttribute("addressId").toString());
            }

            Integer cartId = null;
            if (session.getAttribute("cartId") != null) {
                cartId = helper.tryParseInteger(session.getAttribute("cartId").toString());
            }

            String action = request.getParameter("action");
            if (action == null) {
                action = "";
            }
            if ("paymentSuccess".equals(action) && !"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            switch (action) {
                case "paymentSuccess": {
                    if (!Boolean.parseBoolean(System.getenv("ALLOW_DEMO_CHECKOUT"))) {
                        throw new StoreException("Demo checkout is disabled. Verified online payments are not configured.");
                    }
                    saleService.recordSale(userId, cartId, addressId);
                    response.sendRedirect("checkout?history");
                    return;
                }
                case "paymentFailure": {
                    throw new StoreException("Checkout could not be completed. Confirm the cart again.");
                }
                default: {
                    break;
                }
            }

            if (addressId != null) {
                request.setAttribute("addresses", addressService.findAddressById(addressId));
            } else {
                throw new StoreException("Select an address.");
            }

            request.getRequestDispatcher("pages/payment.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", ex.getMessage());
            response.sendRedirect("cart");
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
            Logger.getLogger(CheckoutServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CheckoutServlet.class.getName()).log(Level.SEVERE, null, ex);
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
