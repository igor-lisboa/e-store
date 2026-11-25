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
import br.uff.store.core.exceptions.PasswordResetRequiredException;
import br.uff.store.core.interfaces.services.IPasswordResetService;
import br.uff.store.application.ResetLinks;
import br.uff.store.core.services.AccountAuthorization;
import br.uff.store.application.security.AccountRedirects;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/account")
public class UserServlet extends HttpServlet {
    private static final String SELUSER = "selUser";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String USERID = "userId";
    private static final String USERROLE = "userRole";
    private static final String REDIRECT = "redirect";
    private final IUserService userService;
    private final IRoleService roleService;
    private final Helper helper;
    private final IPasswordResetService resetService;
    private final String configuredBaseUrl;

    public UserServlet() {
        this(StoreServices.users(), StoreServices.roles(), new Helper(), StoreServices.passwordResets(),
                System.getenv("PUBLIC_BASE_URL"));
    }

    UserServlet(IUserService userService, IRoleService roleService, Helper helper,
            IPasswordResetService resetService, String configuredBaseUrl) {
        this.userService = userService;
        this.roleService = roleService;
        this.helper = helper;
        this.resetService = resetService;
        this.configuredBaseUrl = configuredBaseUrl;
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

        HttpSession session = request.getSession();
        clearLegacyPasswordCookie(request, response);
        try {
            if (request.getParameter("selectedId") != null) {
                session.setAttribute(SELUSER, request.getParameter("selectedId"));
            }

            String action = "";
            if(request.getParameter("action")!=null){
                action = request.getParameter("action");
            }
            if (("save".equals(action) || "login".equals(action) || "register".equals(action)
                    || "logout".equals(action)) && !"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }

            Integer selectedUserId = null;
            if (session.getAttribute(SELUSER) != null) {
                selectedUserId = helper.tryParseInteger(session.getAttribute(SELUSER).toString());
            }

            String email = request.getParameter(EMAIL);
            String password = request.getParameter(PASSWORD);
            
            switch (action) {
                case "save": {
                    Integer activeUserId = helper.tryParseInteger(String.valueOf(session.getAttribute(USERID)));
                    Integer activeRoleId = helper.tryParseInteger(String.valueOf(session.getAttribute(USERROLE)));
                    if (!AccountAuthorization.canEdit(activeUserId, activeRoleId, selectedUserId)) {
                        throw new StoreException("You are not allowed to edit this account.");
                    }
                    Integer roleId = activeRoleId.equals(UserRole.ADMIN.getId())
                            ? helper.tryParseInteger(request.getParameter("roleId"))
                            : userService.findUserById(selectedUserId).getRoleId();
                    userService.saveUser(new UserDTO(
                        selectedUserId,
                        request.getParameter("name"),
                        email,
                        password,
                        roleId
                    ));
                    break;
                }
                case "logout": {

                    session.setAttribute(USERID, null);
                    session.setAttribute(USERROLE, null);
                    session.setAttribute("msg", FlashMessages.text("Signed out successfully.", FlashMessages.localeOf(session)));

                    session.invalidate();
                    response.sendRedirect("");
                    return;
                }
                case "login": {
                    try {
                        UserDTO userDto = userService.login(email, password);
                        request.changeSessionId();
                        session.setAttribute(USERID, userDto.getId());
                            session.setAttribute(USERROLE, userDto.getRoleId());
                            session.setAttribute("msg", FlashMessages.welcome(userDto.getName(), FlashMessages.localeOf(session)));
                            // Remember the email only; never persist a password in a browser cookie.
                            if (request.getParameter("remember") != null) {
                                int rememberEmailSeconds = 2592000;
                                Cookie emailCookie = new Cookie("loginEmail", email);
                                emailCookie.setMaxAge(rememberEmailSeconds);
                                emailCookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                                response.addCookie(emailCookie);
                            }
                    } catch (PasswordResetRequiredException legacyAccount) {
                        // Decision 4A: an account with a legacy password hash must choose a new password by email.
                        try {
                            resetService.requestReset(legacyAccount.getEmail(), ResetLinks.pageUrl(request, configuredBaseUrl));
                            session.setAttribute("msg", FlashMessages.forFailure(legacyAccount, FlashMessages.localeOf(session)));
                        } catch (Exception failure) {
                            session.setAttribute("msg", FlashMessages.forFailure(failure, FlashMessages.localeOf(session)));
                        }
                    } catch (Exception failure) {
                        session.setAttribute("msg", FlashMessages.forFailure(failure, FlashMessages.localeOf(session)));
                    }

                    break;
                }
                case "register": {
                    UserDTO userDto = userService.saveUser(new UserDTO(
                        null,
                        request.getParameter("name"),
                        email,
                        request.getParameter("password"),
                        UserRole.CUSTOMER.getId()
                    ));
                    request.changeSessionId();
                    session.setAttribute(USERID, userDto.getId());
                    session.setAttribute(USERROLE, userDto.getRoleId());
                    session.setAttribute("msg", FlashMessages.welcome(userDto.getName(), FlashMessages.localeOf(session)));
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

            if (request.getAttribute(REDIRECT) != null) {
                redirect = (String) request.getAttribute(REDIRECT);
            } else if (request.getParameter(REDIRECT) != null) {
                redirect = request.getParameter(REDIRECT);
            }
            redirect = AccountRedirects.allowed(redirect);

            if (session.getAttribute(USERID) != null) {

                Integer userId = helper.tryParseInteger(session.getAttribute(USERID).toString());

                if (redirect == null || "null".equals(redirect)) {
                    if (selectedUserId != null) {
                        if (!selectedUserId.equals(userId) && !helper.tryParseInteger(session.getAttribute(USERROLE).toString()).equals(UserRole.ADMIN.getId())) {
                            session.setAttribute(SELUSER, null);
                            throw new StoreException("Access is not permitted.");
                        }

                        request.setAttribute("roles", roleService.listRoles());
                        request.setAttribute("account", userService.findUserById(selectedUserId));
                        redirect = "pages/account-edit.jsp";
                    } else {

                        List<UserDTO> users = new ArrayList<>();

                        if (session.getAttribute(USERROLE).equals(UserRole.ADMIN.getId())) {
                            users = userService.listUsers();
                        } else {
                            users.add(userService.findUserById(userId));
                        }

                        request.setAttribute("users", users);
                        redirect = "pages/accounts.jsp";
                    }
                } else {
                    response.sendRedirect(redirect);
                    return;
                }
            } else {

                request.setAttribute(REDIRECT, redirect);
                redirect = "pages/login.jsp";
            }

            request.getRequestDispatcher(redirect).forward(request, response);
        } catch (Exception failure) {
            session.setAttribute("msg", FlashMessages.forFailure(failure, FlashMessages.localeOf(session)));
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
        } catch (Exception failure) {
            Logger.getLogger(UserServlet.class.getName()).log(Level.SEVERE, null, failure);
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
        } catch (Exception failure) {
            Logger.getLogger(UserServlet.class.getName()).log(Level.SEVERE, null, failure);
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
