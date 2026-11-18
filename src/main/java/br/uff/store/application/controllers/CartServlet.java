package br.uff.store.application.controllers;

import br.uff.store.application.StoreServices;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.services.CartService;
import br.uff.store.core.services.AddressService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private final ICartService cartService;
    private final IAddressService addressService;
    private static final String CART_ID = "cartId";
    private static final String TOTAL_PRICE = "totalPrice";
    private static final String PRODUCTS = "products";
    private final Helper helper;

    public CartServlet() {
        this(StoreServices.carts(), StoreServices.addresses(), new Helper());
    }

    CartServlet(ICartService cartService, IAddressService addressService, Helper helper) {
        this.cartService = cartService;
        this.addressService = addressService;
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
            clearLegacyCartCookie(request, response);

            Integer userId = null;

            if (session.getAttribute("userId") != null) {
                userId = helper.tryParseInteger(session.getAttribute("userId").toString());
            }

            Integer cartId = helper.tryParseInteger(String.valueOf(session.getAttribute(CART_ID)));

            cartId = cartService.getActiveCart(cartId, userId, request.getRemoteAddr()).getId();

            session.setAttribute(CART_ID, cartId);
            
            Integer productId = null;

            if (request.getParameter("addProductId") != null) {
                if (!"POST".equalsIgnoreCase(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                productId = helper.tryParseInteger(request.getParameter("addProductId"));
            }

            if (productId != null) {
                cartService.addProductToCart(cartId, productId);
                session.setAttribute("msg", "Product added to the cart.");
            }

            if (request.getParameter("productId") != null) {
                productId = helper.tryParseInteger(request.getParameter("productId"));
            }

            String action = "";
            if (request.getParameter("action") != null) {
                action = request.getParameter("action");
            }
            if (("removeProduct".equals(action) || "changeQuantity".equals(action))
                    && !"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            switch (action) {
                case "confirmCart": {
                    request.setAttribute("addresses", addressService.listUserAddresses(userId));
                    session.setAttribute(PRODUCTS, cartService.listCartProducts(cartId));
                    session.setAttribute(TOTAL_PRICE, cartService.getCartTotal(cartId));
                    request.getRequestDispatcher("pages/cart-confirmation.jsp").forward(request, response);
                    return;
                }
                case "continueShopping": {

                    response.sendRedirect(PRODUCTS);
                    return;
                }
                case "removeProduct": {
                    cartService.removeProductFromCart(cartId, productId);
                    break;
                }
                case "changeQuantity": {
                    Integer requestedQuantity = null;

                    if (request.getParameter("quantity") != null) {
                        requestedQuantity = helper.tryParseInteger(request.getParameter("quantity"));
                    }

                    cartService.changeProductQuantity(cartId, productId, requestedQuantity);

                    break;
                }
                case "continuePayment": {
                    Integer addressId = null;

                    if (request.getParameter("addressId") != null) {
                        addressId = helper.tryParseInteger(request.getParameter("addressId"));
                    }
                    
                    if (addressId == null) {
                        session.setAttribute("msg", "Select an address.");
                        session.setAttribute("addressId", null);
                        request.setAttribute(TOTAL_PRICE, null);
                        response.sendRedirect("cart");
                        return;
                    } else {
                        session.setAttribute("addressId", addressId);
                    }
                    
                    session.setAttribute(PRODUCTS, cartService.listCartProducts(cartId));
                    
                    request.getRequestDispatcher("checkout").forward(request, response);
                    return;
                }
                default: {
                    break;
                }
            }

            request.setAttribute(PRODUCTS, cartService.listCartProducts(cartId));
            request.setAttribute(TOTAL_PRICE, cartService.getCartTotal(cartId));

            request.getRequestDispatcher("pages/cart.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", ex.getMessage());
            response.sendRedirect(PRODUCTS);
        }
    }

    private void clearLegacyCartCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cartCookie = new Cookie("carrinhoId", "");
        cartCookie.setMaxAge(0);
        cartCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        response.addCookie(cartCookie);
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
            Logger.getLogger(CartServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CartServlet.class.getName()).log(Level.SEVERE, null, ex);
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
