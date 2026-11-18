<%-- 
    Document   : carrinho-confirma
    Created on : 02/10/2019, 11:42:33
    Author     : HP
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.core.dtos.CartItemDTO"%>
<%@page import="br.uff.store.core.dtos.AddressDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="java.util.ArrayList"%>
<%
    // se n tiver um usuario logado chama UserController e configura p redirecionar d volta p CartController
    if (session.getAttribute("userId") == null) {
        session.setAttribute("msg", "Realize seu login para prosseguir.");
        response.sendRedirect("account?redirect=cart");
        return;
    }

    List<AddressDTO> enderecos = new ArrayList<AddressDTO>();
    if (request.getAttribute("enderecos") != null) {
        enderecos = (List<AddressDTO>) request.getAttribute("enderecos");
    }

    List<CartItemDTO> produtos = new ArrayList<CartItemDTO>();
    if (session.getAttribute("products") != null) {
        produtos = (List<CartItemDTO>) session.getAttribute("products");
    }

    double totalPrice = Double.parseDouble(session.getAttribute("totalPrice").toString());
%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Checkout"/>
</jsp:include>

<main class="confirma-container">
    <div class="left-confirma">
        <form method="post" action="cart">
            <h2>Detalhes da entrega</h2>
            <div class="confirma-end">
                <%
                    for (AddressDTO endereco : enderecos) {
                %>
                <div class="confirma-radio" id="check<%= endereco.getId()%>" onclick="radioCheck(<%=endereco.getId()%>)">
                    <div class="radio-container">
                        <input required type="radio" name="addressId" id="end<%=endereco.getId()%>" value="<%= endereco.getId()%>"><label for="end<%= endereco.getId()%>">&nbsp;<%= endereco.getNome()%></label><br>
                    </div>
                    <a href="addresses?selectedId=<%= endereco.getId()%>">Editar</a>
                </div>
                <script>
                    function radioCheck(id) {
                        document.getElementById("addressId" + id).checked = true;
                    }
                </script>
                <%
                    }
                %>
                <div class="confirma-novoend">
                    <a href="addresses?selectedId">Inserir novo endereço</a>
                </div>
            </div>

            <h2>Método de entrega</h2>
            <div class="confirma-frete">
                <div class="confirma-radio">
                    <div class="radio-container">
                        <input type="radio" name="frete" checked>
                        <div class="radio-label">Normal - 5 dias úteis (Frete grátis)</div>
                    </div>
                </div>
            </div>
            <input type="number" style="display:none;" name="totalPrice" value="<%= totalPrice%>"/>
            <button class="confirma-compra" name="action" value="continuePayment">Continuar Pagamento</button>
        </form>
    </div>

    <div class="right-confirma">
        <h2>Resumo do pedido</h2>
        <div class="detalhe-carrinho">
            <div class="detalhe-valor">
                <div class="detalhe-item">
                    <label>Subtotal</label>
                    <div class="detalhe-subtotal"><%= new Helper().tryParseMoneyFormat(totalPrice)%></div>
                </div>
                <div class="detalhe-item">
                    <label>Frete</label>
                    <div class="detalhe-frete">Grátis</div>
                </div>
                <div class="detalhe-item">
                    <label>Total</label>
                    <div class="detalhe-total"><%= new Helper().tryParseMoneyFormat(totalPrice)%></div>
                </div>
                <%
                    for (CartItemDTO produto : produtos) {
                %>
                <div class="detalhe-produto">
                    <div class="detalhe-img">
                        <img alt="<%= produto.getNome()%>" src="<%= produto.getImagem()%>">
                    </div>
                    <div class="detalhe-info">
                        <div class="detalhe-nome">&nbsp;<%= produto.getNome()%></div>
                        <div class="detalhe-id">&nbsp;x&nbsp;<%= produto.getQuantidade()%>&nbsp;Unidade<%= (produto.getQuantidade() > 0 ? "s" : "")%></div>
                    </div>
                </div>
                <%
                    }
                %>
                <div class="detalhe-voltar">
                    <a href="cart">Editar Cart</a>
                </div>
            </div>
        </div>
    </div>
</main>
<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>