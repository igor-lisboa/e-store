package br.uff.store.application.controllers;

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
    private static final String PRODUTOID = "productId";
    private final IProductService produtoService;
    private final IReviewService avaliacaoService;
    private final Helper helper;

    public ProductServlet() {
        helper = new Helper();
        produtoService = new ProductService();
        avaliacaoService = new ReviewService();
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
            // se rating estiver setado na sessao... define null
            session.setAttribute("rating", null);

            // se tem produto selecionado por parametro passa p sessao
            Integer produtoId = null;
            if (request.getParameter(PRODUTOID) != null) {
                produtoId = helper.tryParseInteger(request.getParameter(PRODUTOID));
                session.setAttribute(PRODUTOID, produtoId);
            }

            // se produtoId da sessao for null manda p pag d produtos
            if (session.getAttribute(PRODUTOID) != null) {
                produtoId = helper.tryParseInteger(session.getAttribute(PRODUTOID).toString());
            } else {
                session.setAttribute("msg", "Por favor, selecione um produto.");
                response.sendRedirect("products");
                return;
            }

            // recupera user id
            Integer userId = null;
            if (session.getAttribute("userId") != null) {
                userId = helper.tryParseInteger(session.getAttribute("userId").toString());
            }            

            if (request.getParameter("fav") != null) {
                if (!"POST".equalsIgnoreCase(request.getMethod())) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    return;
                }
                // VERIFICA SE ESTA LOGADO
                if (userId == null) {
                    session.setAttribute("msg", "Realize login para favoritar um produto!");
                    response.sendRedirect("account?redirect=product");
                    return;
                } else {
                    produtoService.usuarioToogleFavoritaProductPorId(produtoId, userId);
                    request.removeAttribute("fav");
                    response.sendRedirect("product");
                    return;
                }
            }
            request.setAttribute("product", produtoService.mostraProductVitrineParaUser(produtoId, userId));
            request.setAttribute("avaliacoes", avaliacaoService.recuperaAvaliacoesDeUmProduct(produtoId));

            request.getRequestDispatcher("pages/product.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", ex.getMessage());
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
        return "Esse servlet trata a visualização do produto para o usuário.";
    }// </editor-fold>

}
