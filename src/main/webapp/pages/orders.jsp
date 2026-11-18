<%-- 
    Document   : compras
    Created on : 02/10/2019, 00:50:48
    Author     : HP
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="br.uff.store.core.dtos.CartItemDTO"%>
<%@page import="br.uff.store.core.dtos.PaginateDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.SaleDTO"%>
<%@page import="java.util.ArrayList"%>
<%
    // se n tiver um usuario logado chama UserController e configura p redirecionar d volta p CheckoutController
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account?redirect=checkout");
    }

    PaginateDTO<List<SaleDTO>> vendas = new PaginateDTO<List<SaleDTO>>();
    if (request.getAttribute("vendas") != null) {
        vendas = (PaginateDTO<List<SaleDTO>>)request.getAttribute("vendas");
    }
%>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Meus Pedidos"/>
</jsp:include>

<h2 class="meus-pedidos">Meus Pedidos</h2>
<%
    for (SaleDTO venda : vendas.getDados()) {
%>
<div class="compras-container">
    <table class="end-grid">
        <caption>Sales</caption>
        <thead>
            <tr class="end-title-container">
                <th scope="col" class="end-title">Nº do Pedido</th>
                <th scope="col" class="end-title">Momento da Checkout</th>
                <th scope="col" class="end-title">Endereço de Entrega</th>
                <th scope="col" class="end-title">Total</th>
            </tr>
        </thead>
        <tbody>
            <tr class="end-data-container">
                <td class="end-data">#<%= venda.getId()%></td>
                <td class="end-data"><%= venda.getCriadoEm()%></td>
                <td class="end-data">
                    <%= venda.getAddress().getLogradouro()%> - <%= venda.getAddress().getCidade()%> - <%= venda.getAddress().getEstado()%> - CEP: <%= venda.getAddress().getCep()%>
                </td>
                <td class="end-data"><%= new Helper().tryParseMoneyFormat(venda.getPrecoTotal())%></td>
            </tr>
            <tr>
                <td colspan="4">
                    <table class="end-grid" style="width: 70%; margin: 0px 15%;">
                        <caption>Products da Sale #<%= venda.getId()%></caption>
                        <thead>
                            <tr class="end-title-container">
                                <th scope="col" class="end-title"></th>
                                <th scope="col" class="end-title">Product</th>
                                <th scope="col" class="end-title">Quantidade</th>
                                <th scope="col" class="end-title">Preço Unitário</th>
                                <th scope="col" class="end-title">Subtotal</th>
                                <th scope="col" class="end-title">Avaliação</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                for (CartItemDTO produto : venda.getProductsDoCart()) {
                            %>
                            <tr class="end-data-container">
                                <td class="end-data"><img alt="<%= produto.getNome()%>" src="<%= produto.getImagem()%>"/></td>
                                <td class="end-data"><a class="end-link" href="product?productId=<%= produto.getProductId()%>"><%= produto.getNome()%></a></td>
                                <td class="end-data"><%= produto.getQuantidade()%></td>
                                <td class="end-data"><%= new Helper().tryParseMoneyFormat(produto.getPreco())%></td>
                                <td class="end-data"><%= new Helper().tryParseMoneyFormat(produto.getPrecoTotal())%></td>
                                <td class="end-data"><a class="end-link" href="reviews?productId=<%= produto.getProductId()%>">Avaliar Product</a></td>
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
    if (vendas.getDados().size() == 0) {
%>
<h2 class="meus-pedidos">Nenhuma compra foi realizada ainda, corra e faça uma compra para ela aparecer aqui!</h2>
<%
    }
%>
<!-- Páginas -->
<div style="font-size: 30px; text-align: center; margin-top: 20px;">
    <% if (vendas.getPaginaAtual() > 1) {%>
    <a href="checkout?history&page=<%= vendas.getPaginaAtual()-1%>">&#8249;</a>
    <% }%>
    <span><%= vendas.getPaginaAtual()%></span>
    <% if (vendas.getPaginaAtual() < vendas.getUltimaPagina()) { %>
    <a href="checkout?history&page=<%= vendas.getPaginaAtual()+1%>">&#8250;</a>
    <% }%>
</div>
<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>