<%-- 
    Document   : conta
    Created on : 28/09/2019, 16:26:16
    Author     : HP
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="br.uff.store.core.dtos.RoleDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.enums.UserRole"%>
<%@page import="br.uff.store.core.dtos.UserDTO"%>
<%
    // se n tiver um usuario logado retorna p userController
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account");
        return;
    }

    if (request.getAttribute("account") == null) {
        response.sendRedirect("account");
        return;
    }

    UserDTO usuario = (UserDTO) request.getAttribute("account");

    List<RoleDTO> permissoes = new ArrayList<RoleDTO>();
    if (request.getAttribute("permissoes") != null) {
        permissoes = (List<RoleDTO>) request.getAttribute("permissoes");
    }
%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Conta Pessoal"/>
</jsp:include>

<div class="user-cadastro-container">

    <h2>Editar Dados</h2>
    <form method="post" action="account">

        <ul class="form-style-1">
            <li>
                <label>Nome Completo </label>
                <input class="field-long" name="name" required type="text" maxlength="255" value="<%= usuario.getNome()%>">
            </li>
            <li>
                <label>E-mail </label>
                <input class="field-long" name="email" required type="email" maxlength="255" value="<%= usuario.getEmail()%>">
            </li>
            <li>
                <label>Senha </label>
                <input class="field-long" name="password" type="password" maxlength="255" autocomplete="new-password" placeholder="Leave blank to keep the current password">
            </li>
            <% if (session.getAttribute("userRole").equals(UserRole.ADM.getId())) { %>
            <li>
                <label>Papel </label>
                <select name="roleId" required>
                    <% for (RoleDTO permissao : permissoes) {%>
                    <option value="<%= permissao.getId()%>" <%= (permissao.getId() == usuario.getPermissaoId() ? "selected" : "")%>><%= permissao.getNome()%></option>
                    <% } %>
                </select>
                <% } else {%>
                <input name="roleId" required type="text" style="display:none;" value="<%= usuario.getPermissaoId()%>">
                <% }%>
            </li>
            <li class="center">
                <button type="submit" name="action" value="clear">Voltar</button>
                <button type="submit" name="action" value="save">Gravar</button>
            </li>
        </ul>

    </form>

</div>

<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>
