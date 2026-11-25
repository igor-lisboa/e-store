<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.core.dtos.AddressDTO"%>
<%@page import="br.uff.store.core.dtos.CartItemDTO"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%

    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account?redirect=cart");
    }

    List<CartItemDTO> products = new ArrayList<CartItemDTO>();
    if (session.getAttribute("products") != null) {
        products = (List<CartItemDTO>) session.getAttribute("products");
    }

    AddressDTO address = null;
    if (request.getAttribute("addresses") != null) {
        address = (AddressDTO) request.getAttribute("addresses");
    }

    java.math.BigDecimal totalPrice = new java.math.BigDecimal(session.getAttribute("totalPrice").toString());
%>

<fmt:message key="ui.pageTitle.demoCheckout" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>

<main class="confirma-container">

    <div class="left-confirma">
        <h2><fmt:message key="ui.checkout"/></h2>
        <% if (Boolean.parseBoolean(System.getenv("ALLOW_DEMO_CHECKOUT"))) { %>
            <p role="note"><fmt:message key="ui.demoNotice"/></p>
            <form action="checkout" method="post">
                <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                <button type="submit" name="action" value="paymentSuccess"><fmt:message key="ui.demoOrder"/></button>
            </form>
        <% } else { %>
            <p role="status"><fmt:message key="ui.paymentsDisabled"/></p>
        <% } %>
    </div>

    <div class="right-confirma">
        <h2><fmt:message key="ui.orderSummary"/></h2>
        <div class="cart-details">
            <div class="value-details">
                <div class="item-details">
                    <label><fmt:message key="ui.subtotal"/></label>
                    <div class="subtotal-details"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(totalPrice)%></div>
                </div>
                <div class="item-details">
                    <label><fmt:message key="ui.shipping"/></label>
                    <div class="shipping-details"><fmt:message key="ui.free"/></div>
                </div>
                <div class="item-details">
                    <label><fmt:message key="ui.total"/></label>
                    <div class="total-details"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(totalPrice)%></div>
                </div>
                <%
                    for (CartItemDTO product : products) {
                %>
                <div class="product-details">
                    <div class="image-details">
                        <img alt="<%= Html.escape(product.getName()) %>" src="<%= Html.escape(product.getImage()) %>">
                    </div>
                    <div class="details-info">
                        <div class="name-details">&nbsp;<%= Html.escape(product.getName()) %></div>
                        <div class="id-details">&nbsp;x&nbsp;<%= product.getQuantity()%>&nbsp;units</div>
                    </div>
                </div>
                <%
                    }
                %>
                <div class="back-details">
                    <a href="cart"><fmt:message key="ui.editCart"/></a>
                </div>
            </div>
        </div>
        <div class="cart-details">
            <div class="value-details">
                <div class="address-details"><fmt:message key="ui.selectedAddress"/></div>
                <div class="address-details-info">
                    <p><%= Html.escape(address.getName()) %></p>
                    <p><%= address.getPostalCode()%></p>
                    <p><%= Html.escape(address.getStreetAddress()) %></p>
                    <p><%= Html.escape(address.getCity()) %>&nbsp;-&nbsp;<%= Html.escape(address.getState()) %></p>
                    <p><%= Html.escape(address.getCountry()) %></p>
                </div>
            </div>
        </div>
        <div class="cart-details">
            <div class="value-details">
                <div class="address-details"><fmt:message key="ui.deliveryMethod"/></div>
                <div class="delivery-details"><fmt:message key="ui.demoDelivery"/></div>
            </div>
        </div>
    </div>

</main>


<jsp:include page="footer.jsp"></jsp:include>
