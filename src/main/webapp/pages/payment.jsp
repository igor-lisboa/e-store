<%-- 
    Document   : paypal
    Created on : 02/10/2019, 02:31:48
    Author     : HP
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.core.dtos.AddressDTO"%>
<%@page import="br.uff.store.core.dtos.CartItemDTO"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%
    // se n tiver um usuario logado chama UserController e configura p redirecionar d volta p CheckoutController
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account?redirect=cart");
    }

    List<CartItemDTO> produtos = new ArrayList<CartItemDTO>();
    if (session.getAttribute("products") != null) {
        produtos = (List<CartItemDTO>) session.getAttribute("products");
    }

    AddressDTO endereco = null;
    if (request.getAttribute("addresses") != null) {
        endereco = (AddressDTO) request.getAttribute("addresses");
    }

    double totalPrice = Double.parseDouble(session.getAttribute("totalPrice").toString());
%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Página de Pagamento"/>
</jsp:include>

<main class="confirma-container">

    <div class="left-confirma">
        <h2>Método de pagamento</h2>

        <div class="confirma-forma-pag">
            <ul class="form-style-1">
                <li>
                    <div id="paypal-button"></div>
                </li>
                <li style="text-align: -webkit-center;">
                    <fieldset>
                        <legend><img alt="paypal" src="img/paypal.png"></legend>
                        <img alt="paypal cartão de crédito" class="credit-card-img" src="img/credit-card.png">
                    </fieldset>
                </li>
                <li>
                    <div class="paypal-sub">Pagamento 100% seguro</div>
                </li>
                <li class="center" style="display: none;">
                    <hr>
                    <fieldset>
                        <legend>Outra forma de pagamento:</legend>
                        <small>Compre apenas clicando no botão!</small>
                        <form action="checkout" method="POST">
                            <button type="submit" name="action" value="paymentSuccess">Checkoutr</button>
                        </form>
                    </fieldset>
                </li>
            </ul>
        </div>

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
        <div class="detalhe-carrinho">
            <div class="detalhe-valor">
                <div class="detalhe-endereco">Endereço de Entrega escolhido</div>
                <div class="detalhe-endereco-info">    
                    <p><%= endereco.getNome()%></p>
                    <p><%= endereco.getCep()%></p>
                    <p><%= endereco.getLogradouro()%></p>
                    <p><%= endereco.getCidade()%>&nbsp;-&nbsp;<%= endereco.getEstado()%></p>
                    <p><%= endereco.getPais()%></p>
                </div>
            </div>
        </div>
        <div class="detalhe-carrinho">
            <div class="detalhe-valor">
                <div class="detalhe-endereco">Método de entrega</div>
                <div class="detalhe-entrega">ENTREGA CONVENCIONAL - Frete grátis para todo o Brasil (Previsão de entrega em 5 dias úteis)</div>
            </div>
        </div>
    </div>

</main>

<script src="https://www.paypalobjects.com/api/checkout.js"></script>
<script>
                                //https://github.com/paypal/paypal-checkout-components/blob/master/docs/implement-checkout.md
                                var btn = paypal.Button.render({
                                    env: "sandbox",
                                    locale: "pt_BR",
                                    client: {
                                        sandbox: "Af4ArltRj8MriojisGcUAhAgTTkSlnxi382830HLHEdiUO5mhlNbCPSIrQ8rQs9mxSrY1aCC2N2q4-8x"
                                    },
                                    commit: true,
                                    style: {
                                        color: "black",
                                        size: "responsive",
                                        shape: "rect",
                                        label: "pay",
                                        tagline: "false"
                                    },
                                    payment: function (data, actions) {
                                        return actions.payment.create({
                                            payment: {
                                                transactions: [{
                                                        amount: {
                                                            total: "<%= totalPrice%>",
                                                            currency: "BRL"
                                                        },
                                                        description: "Checkouts",
                                                        payment_options: {
                                                            allowed_payment_method: "IMMEDIATE_PAY",
                                                        }
                                                    }]
                                            }
                                        });
                                    },
                                    onAuthorize: function (data, actions) {
                                        return actions.payment.execute().then(function (payment) {
                                            console.log("Pagamento OK");
                                            console.log(actions);
                                            var purchaseForm = document.createElement("form");
                                            purchaseForm.method = "POST";
                                            purchaseForm.action = "checkout";
                                            var purchaseAction = document.createElement("input");
                                            purchaseAction.type = "hidden";
                                            purchaseAction.name = "action";
                                            purchaseAction.value = "paymentSuccess";
                                            purchaseForm.appendChild(purchaseAction);
                                            document.body.appendChild(purchaseForm);
                                            purchaseForm.submit();
                                            return false;
                                        });
                                    },
                                    onCancel: function (data, actions) {
                                        window.location.href = "checkout?action=pagamentoErr";
                                        return false;
                                    },
                                    onError: function (err) {
                                        console.log(err);
                                        window.location.href = "checkout?action=pagamentoErr";
                                        return false;
                                    }
                                },
                                        "#paypal-button");


</script>
<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>
