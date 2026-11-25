<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.core.dtos.CartItemDTO"%>
<%@page import="br.uff.store.core.dtos.AddressDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="java.util.ArrayList"%>
<%

    if (session.getAttribute("userId") == null) {
        session.setAttribute("msg", "Realize seu login para prosseguir.");
        response.sendRedirect("account?redirect=cart");
        return;
    }

    List<AddressDTO> addresses = new ArrayList<AddressDTO>();
    if (request.getAttribute("addresses") != null) {
        addresses = (List<AddressDTO>) request.getAttribute("addresses");
    }

    List<CartItemDTO> products = new ArrayList<CartItemDTO>();
    if (session.getAttribute("products") != null) {
        products = (List<CartItemDTO>) session.getAttribute("products");
    }

    java.math.BigDecimal totalPrice = new java.math.BigDecimal(session.getAttribute("totalPrice").toString());
%>

<fmt:message key="ui.pageTitle.checkout" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>

<main class="confirma-container">
    <div class="left-confirma">
        <form method="post" action="cart">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
            <h2><fmt:message key="ui.deliveryDetails"/></h2>
            <div class="confirma-end">
                <%
                    for (AddressDTO address : addresses) {
                %>
                <div class="confirma-radio" id="check<%= address.getId()%>" onclick="radioCheck(<%=address.getId()%>)">
                    <div class="radio-container">
                        <input required type="radio" name="addressId" id="address<%=address.getId()%>" value="<%= address.getId()%>"><label for="address<%= address.getId()%>">&nbsp;<%= Html.escape(address.getName()) %></label><br>
                    </div>
                    <a href="addresses?selectedId=<%= address.getId()%>"><fmt:message key="ui.edit"/></a>
                </div>
                <script>
                    function radioCheck(id) {
                        document.getElementById("address" + id).checked = true;
                    }
                </script>
                <%
                    }
                %>
                <div class="confirma-novoend">
                    <a href="addresses?selectedId"><fmt:message key="ui.addAddress"/></a>
                </div>
            </div>

            <h2><fmt:message key="ui.deliveryMethod"/></h2>
            <div class="confirma-frete">
                <div class="confirma-radio">
                    <div class="radio-container">
                        <input type="radio" name="frete" checked>
                        <div class="radio-label"><fmt:message key="ui.standardDelivery"/></div>
                    </div>
                </div>
            </div>
            <input type="number" style="display:none;" name="totalPrice" value="<%= totalPrice%>"/>
            <button class="confirma-compra" name="action" value="continuePayment"><fmt:message key="ui.continuePayment"/></button>
        </form>
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
    </div>
</main>

<jsp:include page="footer.jsp"></jsp:include>
