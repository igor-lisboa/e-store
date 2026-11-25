<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="br.uff.store.core.enums.UserRole"%>
<%@page import="br.uff.store.core.dtos.UserDTO"%>
<%@page import="java.util.List"%>
<%

    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account");
    }

    List<UserDTO> grid = new ArrayList<UserDTO>();
    if (request.getAttribute("users") != null) {
        grid = (List<UserDTO>) request.getAttribute("users");
    }
%>

<fmt:message key="ui.pageTitle.myAccount" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>
<fmt:message key="ui.js.confirmSignOut" var="js_confirmSignOut"/>

<h2 class="order-history-title"><fmt:message key="ui.myAccount"/></h2>
<div class="main-user-container">

    <div class="left-user-container">
        <div class="left-row">
            <p style="font-weight: bold;"><%= (session.getAttribute("userRole").equals(UserRole.ADMIN.getId()) ? "Lista de Cadastros" : "Meus Dados")%></p>
        </div>
        <%
            for (UserDTO user : grid) {
        %>
        <div class="left-row">
            <p style="padding-bottom: 5px;"><%= Html.escape(user.getEmail()) %></p>
            <div class="edit-row">
                <p><%= Html.escape(user.getName()) %></p>
                <a href="account?selectedId=<%= user.getId()%>"><fmt:message key="ui.edit"/></a>
            </div>
        </div>
        <%
            }
        %>
    </div>

    <div class="right-user-container">
        <div class="right-row">
            <div class="user-info">
                <div class="user-info-middle" onclick="location.href = 'addresses';">
                    <em class="fas fa-map-marker-alt"></em>
                    <p><fmt:message key="ui.addressList"/></p>
                </div>
            </div>
            <div class="user-info">
                <div class="user-info-middle" onclick="location.href = 'products?special=favoritos';">
                    <em class="fas fa-heart"></em>
                    <p><fmt:message key="ui.favorites"/></p>
                </div>
            </div>
            <div class="user-info">
                <div class="user-info-middle" onclick="location.href = 'checkout?history';">
                    <em class="fas fa-shopping-bag"></em>
                    <p><fmt:message key="ui.orderHistory"/></p>
                </div>
            </div>
        </div>
        <div class="right-row">

            <% if (session.getAttribute("userRole").equals(UserRole.ADMIN.getId())) { %>
            <div class="user-info">
                <div class="user-info-middle" onclick="location.href = 'product-admin';">
                    <em class="fas fa-barcode"></em>
                    <p><fmt:message key="ui.manageProducts"/></p>
                </div>
            </div>
            <%
                }
            %>
            <div class="user-info">
                <div class="user-info-middle" onclick="Logout()">
                    <em class="fas fa-sign-out-alt"></em>
                    <p>
                    <form method="post" action="account">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                        <button onclick="return confirm('${js_confirmSignOut}');false;" type="submit" id="logout-btn" name="action" value="logout" formnovalidate><fmt:message key="ui.logout"/></button>
                    </form>
                    </p>
                </div>
                <script>
                    function Logout() {
                        document.getElementById("logout-btn").click();
                    }
                </script>
            </div>
        </div>
    </div>

</div>


<jsp:include page="footer.jsp"></jsp:include>
