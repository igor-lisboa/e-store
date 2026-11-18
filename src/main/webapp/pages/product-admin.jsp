<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.ProductDTO"%>
<%@page import="br.uff.store.core.enums.UserRole"%>
<%@page import="java.util.ArrayList"%>
<%

    if (!session.getAttribute("userRole").equals(UserRole.ADMIN.getId())) {
        response.sendRedirect("account");
    }
    List<ProductDTO> grid = new ArrayList<ProductDTO>();
    if (request.getAttribute("grid") != null) {
        grid = (List<ProductDTO>) request.getAttribute("grid");
    }
%>

<jsp:include page="header.jsp">
    <jsp:param name="title" value="Product administration"/>
</jsp:include>

<table class="product-grid">
    <caption><fmt:message key="ui.products"/></caption>
    <thead>
        <tr>
            <th scope="row" colspan="5"><h2><fmt:message key="ui.products"/></h2></th>
        </tr>
        <tr class="product-grid-title">
            <td><fmt:message key="ui.operations"/></td>
            <td><fmt:message key="ui.name"/></td>
            <td><fmt:message key="ui.price"/></td>
            <td><fmt:message key="ui.category"/></td>
            <td><fmt:message key="ui.stock"/></td>
        </tr>
    </thead>
    <tbody>
        <%
            for (ProductDTO product : grid) {
        %>
        <tr class="product-data">
            <td>
                <a href="product-admin?selectedId=<%= product.getId()%>"><fmt:message key="ui.edit"/></a>
            </td>
            <td><%= Html.escape(product.getName()) %></td>
            <td><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(product.getPrice())%></td>
            <td><%= Html.escape(product.getCategory()) %></td>
            <td><%= product.getQuantity()%></td>
        </tr>
        <%
            }
        %>
    </tbody>
    <tfoot>
        <tr class="product-data-add">
            <td colspan="5">
                <a class="end-link" href="product-admin?selectedId"><fmt:message key="ui.newProduct"/></a>
            </td>
        </tr>
    </tfoot>
</table>


<jsp:include page="footer.jsp"></jsp:include>
