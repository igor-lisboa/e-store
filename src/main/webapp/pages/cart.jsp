<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="br.uff.store.core.dtos.CartItemDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>

<fmt:message key="ui.pageTitle.cart" var="pageTitle"/>
<fmt:message key="ui.quantity" var="quantityLabel"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>
<fmt:message key="ui.js.quantityRange" var="js_quantityRange"/>
<%
    java.math.BigDecimal totalPrice = java.math.BigDecimal.ZERO;
    if (request.getAttribute("totalPrice") != null) {
        totalPrice = new java.math.BigDecimal(request.getAttribute("totalPrice").toString());
    }

    List<CartItemDTO> items = new ArrayList<CartItemDTO>();
    if (request.getAttribute("products") != null) {
        items = (List<CartItemDTO>) request.getAttribute("products");
    }
%>

<h1 class="cart-title"><fmt:message key="ui.cart"/></h1>


<div class="cart-column">
    <label class="cart-img transparent"><fmt:message key="ui.image"/></label>
    <label class="cart-details transparent"><fmt:message key="ui.product"/></label>
    <label class="cart-price"><fmt:message key="ui.price"/></label>
    <label class="cart-qnt"><fmt:message key="ui.quantity"/></label>
    <label class="cart-remove transparent"><fmt:message key="ui.remove"/></label>
    <label class="cart-total"><fmt:message key="ui.total"/></label>
</div>
<%
    if (items.size() < 1) {
%>
<div class="cart-product">
    <h2><fmt:message key="ui.emptyCart"/></h2>
</div>
<%
} else {
    for (CartItemDTO product : items) {
%>

<div class="cart-product">
    <div class="cart-img">
        <img alt="<%= Html.escape(product.getName()) %>" src="<%= Html.escape(product.getImage()) %>">
    </div>
    <div class="cart-details">
        <div class="cart-name"><%= Html.escape(product.getName()) %></div>
        <p class="cart-desc"><%= Html.escape(product.getDescription()) %></p>
    </div>
    <div class="cart-price"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(product.getPrice())%></div>
    <div class="cart-qnt">
        <form action="cart?productId=<%= product.getProductId()%>&action=changeQuantity" method="post" id="quantityForm<%= product.getProductId()%>">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
            <input aria-label="${quantityLabel}: <%= Html.escape(product.getName()) %>" style="width:50px;" type="number" min="1" max="<%= product.getAvailableStock()%>" value="<%= product.getQuantity()%>" name="quantity" onchange="updateQuantity('quantityForm<%= product.getProductId()%>')"/>
        </form>
    </div>
    <div class="cart-remove">
        <form action="cart?productId=<%= product.getProductId()%>" method="post">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
            <button class="cart-remove-btn" name="action" value="removeProduct"><fmt:message key="ui.remove"/></button>
        </form>
    </div>
    <div class="cart-total"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(product.getTotalPrice())%></div>
</div>
<%
        }
    }
%>

<div class="total-finalize">
    <%
        if (items.size() > 0) {
    %>
    <div class="total-item">
        <p><fmt:message key="ui.freeShipping"/></p>
        <label><fmt:message key="ui.subtotal"/></label>
        <div class="total-subtotal"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(totalPrice)%></div>
    </div>
    <div class="total-item">
        <label><fmt:message key="ui.shipping"/></label>
        <div class="total-ship"><fmt:message key="ui.free"/></div>
    </div>
    <div class="total-item">
        <label><fmt:message key="ui.total"/></label>
        <div class="total-total"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(totalPrice)%></div>
    </div>
    <%
        }
    %>
    <div class="total-item">
        <form method="post" action="cart">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
            <input type="number" style="display:none;" name="totalPrice" value="<%= totalPrice%>"/>
            <button class="total-keep" name="action" value="continueShopping" type="submit"><fmt:message key="ui.continueShopping"/></button>
            <%
                if (items.size() > 0) {
            %>
            <button class="total-checkout" name="action" value="confirmCart" type="submit"><fmt:message key="ui.confirmCart"/></button>
            <%
                }
            %>
        </form>
    </div>
</div>
<script>
    function updateQuantity(formId) {
        var form = document.getElementById(formId);
        var isValidForm = form.checkValidity();
        if (isValidForm) {
            form.submit();
        } else {
            alert("${js_quantityRange}");
            return false;
        }
    }
</script>

<jsp:include page="footer.jsp"></jsp:include>
