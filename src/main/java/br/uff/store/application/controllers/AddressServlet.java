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

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.services.AddressService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/addresses")
public class AddressServlet extends HttpServlet {
    private final IAddressService enderecoService;
    private final Helper helper;

    public AddressServlet() {
        helper = new Helper();
        enderecoService = new AddressService();
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
            Integer userId = null;
            // recupera usuario logado
            if (session.getAttribute("userId") != null) {
                userId = helper.tryParseInteger(session.getAttribute("userId").toString());
            } else {
                response.sendRedirect("account?redirect=addresses");
                return;
            }

            if (request.getParameter("selectedId") != null) {
                String enderecoIdSelStr = request.getParameter("selectedId");
                Integer enderecoIdSel = helper.tryParseInteger(enderecoIdSelStr);
                AddressDTO endereco = new AddressDTO("", userId, null, "", "", "", "Brasil");
                if(enderecoIdSel != null) {
                    endereco = enderecoService.encontraAddressPorId(enderecoIdSel);
                    if (!enderecoService.verificaAddressDoUser(enderecoIdSel, userId)) {
                        throw new IllegalArgumentException("The address does not belong to this account.");
                    }
                }
                request.setAttribute("addresses", endereco);
                session.setAttribute("selectedId", enderecoIdSelStr);
                request.getRequestDispatcher("pages/address-edit.jsp").forward(request, response);
                return;
            }

            // define endereco selecionado
            Integer enderecoIdSel = null;
            if (session.getAttribute("selectedId") != null) {
                enderecoIdSel = helper.tryParseInteger(session.getAttribute("selectedId").toString());
            }

            // recupera acao solicitada se existir
            String action = "";

            if (request.getParameter("action") != null) {
                action = request.getParameter("action");
            }
            if (("save".equals(action) || "delete".equals(action))
                    && !"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            switch (action) {
                case "save": {
                    AddressDTO enderecoDto = new AddressDTO(
                        request.getParameter("name"),
                        userId,
                        helper.tryParseInteger(request.getParameter("zipcode")),
                        request.getParameter("address"),
                        request.getParameter("city"),
                        request.getParameter("state"),
                        request.getParameter("country")
                    );

                    if (enderecoIdSel != null) {
                        enderecoService.atualizaAddressPorId(enderecoIdSel, enderecoDto);
                    } else {
                        enderecoService.insereAddress(enderecoDto);
                    }

                    session.setAttribute("msg", "Endereço gravado com sucesso!");
                    break;
                }
                case "delete": {
                    enderecoService.excluiAddressPorId(enderecoIdSel, userId);

                    session.setAttribute("msg", "Endereço deletado com sucesso!");
                    break;
                }
                case "clear": {
                    // apaga sel da sessao
                    session.setAttribute("selectedId", null);
                    break;
                }
                default: {
                    break;
                }
            }

            request.setAttribute("grid", enderecoService.listaAddresssPorUserId(userId));

            // manda p pag de grid de produtos
            request.getRequestDispatcher("pages/addresses.jsp").forward(request, response);
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
            Logger.getLogger(AddressServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(AddressServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet pra tratar interações com endereços.";
    }// </editor-fold>

}
