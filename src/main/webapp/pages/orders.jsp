<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="br.uff.store.core.dtos.SaleLineDTO"%>
<%@page import="br.uff.store.core.dtos.PaginationDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.SaleDTO"%>
<%@page import="java.util.ArrayList"%>
<%

    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account?redirect=checkout");
    }

    PaginationDTO<List<SaleDTO>> sales = new PaginationDTO<List<SaleDTO>>();
    if (request.getAttribute("sales") != null) {
        sales = (PaginationDTO<List<SaleDTO>>)request.getAttribute("sales");
    }
%>
<fmt:message key="ui.pageTitle.orders" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>

<h2 class="order-history-title"><fmt:message key="ui.myOrders"/></h2>
<%
    for (SaleDTO sale : sales.getItems()) {
%>
<div class="orders-container">
    <table class="end-grid">
        <caption><fmt:message key="ui.orders"/></caption>
        <thead>
            <tr class="end-title-container">
                <th scope="col" class="end-title"><fmt:message key="ui.orderNumber"/></th>
                <th scope="col" class="end-title"><fmt:message key="ui.orderDate"/></th>
                <th scope="col" class="end-title"><fmt:message key="ui.deliveryAddress"/></th>
                <th scope="col" class="end-title"><fmt:message key="ui.total"/></th>
                <th scope="col" class="end-title"><fmt:message key="ui.orderStatus"/></th>
            </tr>
        </thead>
        <tbody>
            <tr class="end-data-container">
                <td class="end-data">#<%= sale.getId()%></td>
                <td class="end-data"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).formatDate(sale.getCreatedAt()) %></td>
                <td class="end-data">
                    <%= Html.escape(sale.getAddress().getStreetAddress()) %> - <%= Html.escape(sale.getAddress().getCity()) %> - <%= Html.escape(sale.getAddress().getState()) %> - CEP: <%= sale.getAddress().getPostalCode()%>
                </td>
                <td class="end-data"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(sale.getTotalPrice())%></td>
                <td class="end-data">
                    <span class="order-status">
                    <% if ("CANCELLED".equals(sale.getStatus())) { %>
                    <fmt:message key="ui.orderCancelled"/>
                    <% } else if ("SHIPPED".equals(sale.getStatus())) { %>
                    <fmt:message key="ui.orderShipped"/>
                    <% } else { %>
                    <fmt:message key="ui.orderPlaced"/>
                    <% } %>
                    </span>
                    <% if (sale.isCancellable()) { %>
                    <form action="checkout" method="post" onsubmit="return confirm('<fmt:message key="ui.confirmCancelOrder"/>');">
                        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                        <input type="hidden" name="orderId" value="<%= sale.getId()%>">
                        <button type="submit" name="action" value="cancelOrder"><fmt:message key="ui.cancelOrder"/></button>
                    </form>
                    <% } %>
                </td>
            </tr>
            <tr>
                <td colspan="5">
                    <table class="end-grid" style="width: 70%; margin: 0px 15%;">
                        <caption>Products da Sale #<%= sale.getId()%></caption>
                        <thead>
                            <tr class="end-title-container">
                                <th scope="col" class="end-title"></th>
                                <th scope="col" class="end-title"><fmt:message key="ui.product"/></th>
                                <th scope="col" class="end-title"><fmt:message key="ui.quantity"/></th>
                                <th scope="col" class="end-title"><fmt:message key="ui.unitPrice"/></th>
                                <th scope="col" class="end-title"><fmt:message key="ui.subtotal"/></th>
                                <th scope="col" class="end-title"><fmt:message key="ui.rating"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                for (SaleLineDTO line : sale.getLines()) {
                            %>
                            <tr class="end-data-container">
                                <td class="end-data"><% if (line.getImage() != null) { %><img alt="<%= Html.escape(line.getProductName()) %>" src="<%= Html.escape(line.getImage()) %>"/><% } %></td>
                                <td class="end-data">
                                    <% if (line.getProductId() != null) { %>
                                    <a class="end-link" href="product?productId=<%= line.getProductId()%>"><%= Html.escape(line.getProductName()) %></a>
                                    <% } else { %>
                                    <%= Html.escape(line.getProductName()) %>
                                    <% } %>
                                </td>
                                <td class="end-data"><%= line.getQuantity()%></td>
                                <td class="end-data"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(line.getUnitPrice())%></td>
                                <td class="end-data"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(line.getLineTotal())%></td>
                                <td class="end-data"><% if (line.getProductId() != null) { %><a class="end-link" href="reviews?productId=<%= line.getProductId()%>"><fmt:message key="ui.reviewProduct"/></a><% } %></td>
                            </tr>
                            <%
                                }
                            %>
                        </tbody>
                    </table>
                </td>
            </tr>
        </tbody>
    </table>
</div>

<%
    }
    if (sales.getItems().size() == 0) {
%>
<h2 class="order-history-title"><fmt:message key="ui.emptyOrders"/></h2>
<%
    }
%>

<div style="font-size: 30px; text-align: center; margin-top: 20px;">
    <% if (sales.getPageNumber() > 1) {%>
    <a href="checkout?history&page=<%= sales.getPageNumber()-1%>">&#8249;</a>
    <% }%>
    <span><%= sales.getPageNumber()%></span>
    <% if (sales.getPageNumber() < sales.getLastPage()) { %>
    <a href="checkout?history&page=<%= sales.getPageNumber()+1%>">&#8250;</a>
    <% }%>
</div>

<jsp:include page="footer.jsp"></jsp:include>
