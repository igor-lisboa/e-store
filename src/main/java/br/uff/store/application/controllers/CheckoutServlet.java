package br.uff.store.application.controllers;

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

import br.uff.store.core.dtos.PaginateDTO;
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
    private final ISaleService vendaService;
    private final ICartService carrinhoService;
    private final IAddressService enderecoService;
    private final Helper helper;

    public CheckoutServlet() {
        vendaService = new SaleService();
        carrinhoService = new CartService();
        enderecoService = new AddressService();
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
        // pega sessao
        HttpSession session = request.getSession();
        try {
            // se n tem usuario logado manda p controller de user
            Integer usuarioId = null;
            if (session.getAttribute("userId") == null) {
                response.sendRedirect("account?redirect=cart");
                return;
            } else {
                usuarioId = helper.tryParseInteger(session.getAttribute("userId").toString());
            }

            // se solicitar historico manda p pag d compras realizadas
            if (request.getParameter("history") != null) {
                Integer paginaAtual = 1;
                if (request.getParameter("page") != null) {
                    paginaAtual = helper.tryParseInteger(request.getParameter("page"));
                }

                PaginateDTO<List<SaleDTO>> vendas = vendaService.listaSalesDoUser(usuarioId, 5, paginaAtual);
                for(SaleDTO venda : vendas.getDados()) {
                    venda.setProductsDoCart(carrinhoService.listaProductsCart(venda.getCartId()));
                    venda.setAddress(enderecoService.encontraAddressPorId(venda.getAddressId()));
                }
                request.setAttribute("vendas", vendas);

                request.getRequestDispatcher("pages/orders.jsp").forward(request, response);
                return;
            }

            Integer enderecoId = null;
            if (session.getAttribute("enderecoId") != null) {
                enderecoId = helper.tryParseInteger(session.getAttribute("enderecoId").toString());
            }

            Integer carrinhoId = null;
            if (session.getAttribute("carrinhoId") != null) {
                carrinhoId = helper.tryParseInteger(session.getAttribute("carrinhoId").toString());
            }

            // recupera acao solicitada se existir
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
                    vendaService.gravaSale(usuarioId, carrinhoId, enderecoId);
                    response.sendRedirect("checkout?history");
                    return;
                }
                case "paymentFailure": {
                    throw new StoreException("Houve um erro durante sua compra, realize o procedimento de confirmação de carrinho novamente.");
                }
                default: {
                    break;
                }
            }

            if (enderecoId != null) {
                request.setAttribute("addresses", enderecoService.encontraAddressPorId(enderecoId));
            } else {
                throw new StoreException("Por favor selecione um endereço!");
            }

            // manda atributos para a pagina definida, no caso carrinho.jsp
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
        return "Servlet pra tratar compras.";
    }// </editor-fold>

}
