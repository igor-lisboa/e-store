<%-- 
    Document   : carrinho
    Created on : 13/09/2019, 02:33:53
    Author     : Caio
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="br.uff.store.core.dtos.CartItemDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Cart"/>
</jsp:include>
<%
    double totalPrice = 0;
    if (request.getAttribute("totalPrice") != null) {
        totalPrice = Double.parseDouble(request.getAttribute("totalPrice").toString());
    }

    List<CartItemDTO> itens = new ArrayList<CartItemDTO>();
    if (request.getAttribute("products") != null) {
        itens = (List<CartItemDTO>) request.getAttribute("products");
    }
%>
<!-- Título da página -->
<h1 class="cart-title">Cart</h1>

<!-- Coluna de labels -->
<div class="cart-column">
    <label class="cart-img transparent">Imagem</label>
    <label class="cart-details transparent">Product</label>
    <label class="cart-price">Preço</label>
    <label class="cart-qnt">Quantidade</label>
    <label class="cart-remove transparent">Remover</label>
    <label class="cart-total">Total</label>
</div>
<%
    if (itens.size() < 1) {
%>
<div class="cart-product">
    <h2>Seu carrinho está vazio por enquanto.</h2>
</div>
<%
} else {
    for (CartItemDTO produto : itens) {
%>
<!-- Product no carrinho -->
<div class="cart-product">
    <div class="cart-img">
        <img alt="<%= produto.getNome()%>" src="<%= produto.getImagem()%>">
    </div>
    <div class="cart-details">
        <div class="cart-name"><%= produto.getNome()%></div>
        <p class="cart-desc"><%= produto.getDescricao()%></p>
    </div>
    <div class="cart-price"><%= new Helper().tryParseMoneyFormat(produto.getPreco())%></div>
    <div class="cart-qnt">
        <form action="cart?productId=<%= produto.getProductId()%>&action=mudaQtd" method="post" id="mudaQtdForm<%= produto.getProductId()%>">
            <input style="width:50px;" type="number" min="1" max="<%= produto.getQuantidadeEmEstoque()%>" value="<%= produto.getQuantidade()%>" name="quantity" onchange="mudaQtd('mudaQtdForm<%= produto.getProductId()%>')"/>
        </form>
    </div>
    <div class="cart-remove">
        <form action="cart?productId=<%= produto.getProductId()%>" method="post">
            <button class="cart-remove-btn" name="action" value="removeProduct">Remover</button>
        </form>
    </div>
    <div class="cart-total"><%= new Helper().tryParseMoneyFormat(produto.getPrecoTotal())%></div>
</div>
<%
        }
    }
%>
<!-- Checkout -->
<div class="total-finalize">
    <%
        if (itens.size() > 0) {
    %>
    <div class="total-item">
        <p>Frete grátis para todo Brasil!</p>
        <label>Subtotal</label>
        <div class="total-subtotal"><%= new Helper().tryParseMoneyFormat(totalPrice)%></div>
    </div>
    <div class="total-item">
        <label>Frete</label>
        <div class="total-ship">Grátis</div>
    </div>
    <div class="total-item">
        <label>Total</label>
        <div class="total-total"><%= new Helper().tryParseMoneyFormat(totalPrice)%></div>
    </div>
    <%
        }
    %>
    <div class="total-item">
        <form method="post" action="cart">
            <input type="number" style="display:none;" name="totalPrice" value="<%= totalPrice%>"/>
            <button class="total-keep" name="action" value="continueShopping" type="submit">Continuar Checkoutndo</button>
            <%
                if (itens.size() > 0) {
            %>
            <button class="total-checkout" name="action" value="confirmCart" type="submit">Finalizar Checkout</button>
            <%
                }
            %>
        </form>
    </div>
</div>
<script>
    function mudaQtd(formId) {
        var form = document.getElementById(formId);
        var isValidForm = form.checkValidity();
        if (isValidForm) {
            form.submit();
        } else {
            alert("Por favor insira uma quantidade válida! A quantidade atual está acima da quantidade do produto em estoque ou abaixo da quantidade mínima.");
            return false;
        }
    }
</script>
<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>