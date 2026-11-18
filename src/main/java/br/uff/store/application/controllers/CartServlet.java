package br.uff.store.application.controllers;

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
    private final ICartService carrinhoService;
    private final IAddressService enderecoService;
    private static final String CARRINHOID = "carrinhoId";
    private static final String PRECOTOTAL = "totalPrice";
    private static final String PRODUTOS = "products";
    private final Helper helper;

    public CartServlet() {
        this(new CartService(), new AddressService(), new Helper());
    }

    CartServlet(ICartService carrinhoService, IAddressService enderecoService, Helper helper) {
        this.carrinhoService = carrinhoService;
        this.enderecoService = enderecoService;
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
        // pega sessao
        HttpSession session = request.getSession();
        try {
            clearLegacyCartCookie(request, response);

            Integer userId = null;
            // recupera userId
            if (session.getAttribute("userId") != null) {
                userId = helper.tryParseInteger(session.getAttribute("userId").toString());
            }

            Integer carrinhoId = helper.tryParseInteger(String.valueOf(session.getAttribute(CARRINHOID)));

            carrinhoId = carrinhoService.recuperaCartAtivo(carrinhoId, userId, request.getRemoteAddr()).getId();

            session.setAttribute(CARRINHOID, carrinhoId);
            
            Integer produtoId = null;
            // recupera produtoId pra add no carrinho
            if (request.getParameter("addProductId") != null) {
                if (!"POST".equalsIgnoreCase(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                produtoId = helper.tryParseInteger(request.getParameter("addProductId"));
            }

            // se produtoId nao for null add
            if (produtoId != null) {
                carrinhoService.insereProductCart(carrinhoId, produtoId);
                session.setAttribute("msg", "Product inserido no carrinho com sucesso!");
            }

            // recupera produtoId do request parameter
            if (request.getParameter("productId") != null) {
                produtoId = helper.tryParseInteger(request.getParameter("productId"));
            }

            // recupera acao solicitada se existir
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
                    request.setAttribute("enderecos", enderecoService.listaAddresssPorUserId(userId));
                    session.setAttribute(PRODUTOS, carrinhoService.listaProductsCart(carrinhoId));
                    session.setAttribute(PRECOTOTAL, carrinhoService.recuperaPrecoTotalDeUmCart(carrinhoId));
                    request.getRequestDispatcher("pages/cart-confirmation.jsp").forward(request, response);
                    return;
                }
                case "continueShopping": {
                    // redireciona p controller de ProductsController
                    response.sendRedirect(PRODUTOS);
                    return;
                }
                case "removeProduct": {
                    carrinhoService.removeProductCart(carrinhoId, produtoId);
                    break;
                }
                case "changeQuantity": {
                    Integer qtdProduct = null;
                    // recupera qtdProduct
                    if (request.getParameter("quantity") != null) {
                        qtdProduct = helper.tryParseInteger(request.getParameter("quantity"));
                    }

                    carrinhoService.alteraQuantidadeProductCart(carrinhoId, produtoId, qtdProduct);

                    break;
                }
                case "continuePayment": {
                    Integer enderecoId = null;
                    // recupera enderecoId
                    if (request.getParameter("addressId") != null) {
                        enderecoId = helper.tryParseInteger(request.getParameter("addressId"));
                    }
                    
                    if (enderecoId == null) {
                        session.setAttribute("msg", "Por favor selecione um endereço!");
                        session.setAttribute("enderecoId", null);
                        request.setAttribute(PRECOTOTAL, null);
                        response.sendRedirect("cart");
                        return;
                    } else {
                        session.setAttribute("enderecoId", enderecoId);
                    }
                    
                    session.setAttribute(PRODUTOS, carrinhoService.listaProductsCart(carrinhoId));
                    
                    request.getRequestDispatcher("checkout").forward(request, response);
                    return;
                }
                default: {
                    break;
                }
            }

            request.setAttribute(PRODUTOS, carrinhoService.listaProductsCart(carrinhoId));
            request.setAttribute(PRECOTOTAL, carrinhoService.recuperaPrecoTotalDeUmCart(carrinhoId));

            // manda atributos para a pag do carrinho
            request.getRequestDispatcher("pages/cart.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", ex.getMessage());
            response.sendRedirect(PRODUTOS);
        }
    }

    private void clearLegacyCartCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cartCookie = new Cookie(CARRINHOID, "");
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
        return "Servlet pra tratar ações no carrinho.";
    }// </editor-fold>

}
