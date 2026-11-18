<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="br.uff.store.core.dtos.RoleDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.enums.UserRole"%>
<%@page import="br.uff.store.core.dtos.UserDTO"%>
<%

    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account");
        return;
    }

    if (request.getAttribute("account") == null) {
        response.sendRedirect("account");
        return;
    }

    UserDTO user = (UserDTO) request.getAttribute("account");

    List<RoleDTO> roles = new ArrayList<RoleDTO>();
    if (request.getAttribute("roles") != null) {
        roles = (List<RoleDTO>) request.getAttribute("roles");
    }
%>

<jsp:include page="header.jsp">
    <jsp:param name="title" value="Account details"/>
</jsp:include>

<div class="account-details-container">

    <h2><fmt:message key="ui.editAccount"/></h2>
    <form method="post" action="account">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

        <ul class="form-style-1">
            <li>
                <label><fmt:message key="ui.fullName"/></label>
                <input class="field-long" name="name" required type="text" maxlength="255" value="<%= Html.escape(user.getName()) %>">
            </li>
            <li>
                <label>E-mail </label>
                <input class="field-long" name="email" required type="email" maxlength="255" value="<%= Html.escape(user.getEmail()) %>">
            </li>
            <li>
                <label><fmt:message key="ui.password"/></label>
                <input class="field-long" name="password" type="password" maxlength="255" autocomplete="new-password" placeholder="Leave blank to keep the current password">
            </li>
            <% if (session.getAttribute("userRole").equals(UserRole.ADMIN.getId())) { %>
            <li>
                <label><fmt:message key="ui.role"/></label>
                <select name="roleId" required>
                    <% for (RoleDTO role : roles) {%>
                    <option value="<%= role.getId()%>" <%= (role.getId() == user.getRoleId() ? "selected" : "")%>><%= Html.escape(role.getName()) %></option>
                    <% } %>
                </select>
                <% } else {%>
                <input name="roleId" required type="text" style="display:none;" value="<%= user.getRoleId()%>">
                <% }%>
            </li>
            <li class="center">
                <button type="submit" name="action" value="clear"><fmt:message key="ui.back"/></button>
                <button type="submit" name="action" value="save"><fmt:message key="ui.save"/></button>
            </li>
        </ul>

    </form>

</div>


<jsp:include page="footer.jsp"></jsp:include>
