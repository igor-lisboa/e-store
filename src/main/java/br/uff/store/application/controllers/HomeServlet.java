package br.uff.store.application.controllers;

import br.uff.store.application.FlashMessages;
import br.uff.store.application.StoreServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.services.ProductService;

@WebServlet("")
public class HomeServlet extends HttpServlet {
    private final IProductService productService;

    public HomeServlet() {
        productService = StoreServices.products();
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        List<FeaturedProductDTO> jogosVitrine = new ArrayList<>();

        try {
            jogosVitrine = productService.listFeaturedProducts();
        } catch (Exception e) {
            session.setAttribute("msg", FlashMessages.forFailure(e, FlashMessages.localeOf(session)));
        }

        request.setAttribute("jogosVitrine", jogosVitrine);
        request.getRequestDispatcher("pages/home.jsp").forward(request, response);
    }

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
            Logger.getLogger(HomeServlet.class.getName()).log(Level.SEVERE, null, ex);
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
