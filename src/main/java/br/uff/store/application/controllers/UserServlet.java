package br.uff.store.application.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.enums.UserRole;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IRoleService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.core.services.RoleService;
import br.uff.store.core.services.UserService;
import br.uff.store.core.services.AccountAuthorization;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/account")
public class UserServlet extends HttpServlet {
    private static final String SELUSER = "selUser";
    private static final String EMAIL = "email";
    private static final String SENHA = "password";
    private static final String USERID = "userId";
    private static final String USERROLE = "userRole";
    private static final String REDIRECT = "redirect";
    private final IUserService usuarioService;
    private final IRoleService roleService;
    private final Helper helper;

    public UserServlet() {
        usuarioService = new UserService();
        roleService = new RoleService();
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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, StoreException {
        // pega sessao
        HttpSession session = request.getSession();
        clearLegacyPasswordCookie(request, response);
        try {
            if (request.getParameter("selectedId") != null) {
                session.setAttribute(SELUSER, request.getParameter("selectedId"));
            }

            // recupera acao solicitada se existir
            String action = "";
            if(request.getParameter("action")!=null){
                action = request.getParameter("action");
            }
            if (("save".equals(action) || "login".equals(action) || "register".equals(action)
                    || "logout".equals(action)) && !"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            Integer usuarioIdSelectionado = null;
            if (session.getAttribute(SELUSER) != null) {
                usuarioIdSelectionado = helper.tryParseInteger(session.getAttribute(SELUSER).toString());
            }

            String email = request.getParameter(EMAIL);
            String senha = request.getParameter(SENHA);
            
            switch (action) {
                case "save": {
                    Integer activeUserId = helper.tryParseInteger(String.valueOf(session.getAttribute(USERID)));
                    Integer activeRoleId = helper.tryParseInteger(String.valueOf(session.getAttribute(USERROLE)));
                    if (!AccountAuthorization.canEdit(activeUserId, activeRoleId, usuarioIdSelectionado)) {
                        throw new StoreException("You are not allowed to edit this account.");
                    }
                    Integer roleId = activeRoleId.equals(UserRole.ADM.getId())
                            ? helper.tryParseInteger(request.getParameter("roleId"))
                            : usuarioService.encontraUserPorId(usuarioIdSelectionado).getPermissaoId();
                    usuarioService.gravaUser(new UserDTO(
                        usuarioIdSelectionado,
                        request.getParameter("name"),
                        email,
                        senha,
                        roleId
                    ));
                    break;
                }
                case "logout": {
                    // define variaveis de sessao d usuario como null
                    session.setAttribute(USERID, null);
                    session.setAttribute(USERROLE, null);
                    session.setAttribute("msg", "Usuário deslogado com sucesso!");
                    // invalida sessao
                    session.invalidate();
                    response.sendRedirect("");
                    return;
                }
                case "login": {
                    try {
                        UserDTO usuarioDto = usuarioService.login(email, senha);
                        request.changeSessionId();
                        session.setAttribute(USERID, usuarioDto.getId());
                            session.setAttribute(USERROLE, usuarioDto.getPermissaoId());
                            session.setAttribute("msg", "Seja bem vindo " + usuarioDto.getNome() + "!");
                            // Remember the email only; never persist a password in a browser cookie.
                            if (request.getParameter("remember") != null) {
                                int durMes = 2592000;
                                Cookie ckEmail = new Cookie("loginEmail", email);
                                ckEmail.setMaxAge(durMes);
                                ckEmail.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                                response.addCookie(ckEmail);
                            }
                    } catch (Exception el) {
                        session.setAttribute("msg", el.getMessage());
                    }

                    break;
                }
                case "register": {
                    UserDTO usuarioDto = usuarioService.gravaUser(new UserDTO(
                        null,
                        request.getParameter("name"),
                        email,
                        request.getParameter("password"),
                        UserRole.CLIENTE.getId()
                    ));
                    request.changeSessionId();
                    session.setAttribute(USERID, usuarioDto.getId());
                    session.setAttribute(USERROLE, usuarioDto.getPermissaoId());
                    session.setAttribute("msg", "Seja bem vindo " + usuarioDto.getNome() + "!");
                    break;
                }
                case "clear": {
                    session.setAttribute(SELUSER, null);
                    response.sendRedirect("account");
                    return;
                }
                default: {
                    break;
                }
            }

            String redirect = null;
            // recupera redirect
            if (request.getAttribute(REDIRECT) != null) {
                redirect = (String) request.getAttribute(REDIRECT);
            } else if (request.getParameter(REDIRECT) != null) {
                redirect = request.getParameter(REDIRECT);
            }

            // se tem usuario logado manda p conta caso contrario p login
            if (session.getAttribute(USERID) != null) {
                //recupera userId da sessao
                Integer userId = helper.tryParseInteger(session.getAttribute(USERID).toString());
                // define redirect se n foi passado
                if (redirect == null || "null".equals(redirect)) {
                    if (usuarioIdSelectionado != null) {
                        if (!usuarioIdSelectionado.equals(userId) && !helper.tryParseInteger(session.getAttribute(USERROLE).toString()).equals(UserRole.ADM.getId())) {
                            session.setAttribute(SELUSER, null);
                            throw new StoreException("Acesso não permitido!");
                        }

                        request.setAttribute("permissoes", roleService.listaRoles());
                        request.setAttribute("account", usuarioService.encontraUserPorId(usuarioIdSelectionado));
                        redirect = "pages/account-edit.jsp";
                    } else {
                        // define grid
                        List<UserDTO> usuarios = new ArrayList<>();

                        // se for adm traz td mundo
                        if (session.getAttribute(USERROLE).equals(UserRole.ADM.getId())) {
                            usuarios = usuarioService.listaUsers();
                        } else {
                            usuarios.add(usuarioService.encontraUserPorId(userId));
                        }

                        request.setAttribute("usuarios", usuarios);
                        redirect = "pages/accounts.jsp";
                    }
                } else {
                    response.sendRedirect(redirect);
                    return;
                }
            } else {
                // define redirect q sera passado p pag d login
                request.setAttribute(REDIRECT, redirect);
                redirect = "pages/login.jsp";
            }

            request.getRequestDispatcher(redirect).forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", ex.getMessage());
            response.sendRedirect("");
        }
    }

    private void clearLegacyPasswordCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie passwordCookie = new Cookie("loginPassword", "");
        passwordCookie.setMaxAge(0);
        passwordCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        response.addCookie(passwordCookie);
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
            Logger.getLogger(UserServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(UserServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet pra tratar edição de usuário.";
    }// </editor-fold>

}
