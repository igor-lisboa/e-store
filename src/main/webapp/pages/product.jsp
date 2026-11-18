<%-- 
    Document   : template-produto
    Created on : 13/09/2019, 18:51:42
    Author     : Caio
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.ProductReviewListDTO"%>
<%@page import="br.uff.store.core.dtos.CustomerProductDTO"%>
<%@page import="java.util.ArrayList"%>
<%
    // recupera produtoId
    String produtoId = "";
    if (session.getAttribute("productId") != null) {
        produtoId = session.getAttribute("productId").toString();
    } else {
        response.sendRedirect("product");
    }

    CustomerProductDTO produto = new CustomerProductDTO();
    List<ProductReviewListDTO> avaliacoes = new ArrayList<ProductReviewListDTO>();
    if (request.getAttribute("product") != null) {
        produto = (CustomerProductDTO) request.getAttribute("product");
        avaliacoes = (List<ProductReviewListDTO>) request.getAttribute("avaliacoes");
    } else {
        session.setAttribute("msg", "Por favor selecione um produto!");
        response.sendRedirect("products");
        return;
    }
%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="<%= produto.getNome()%>"/>
</jsp:include>

<main class="container">

    <!-- Coluna da esquerda / Imagem do produto -->
    <div class="left-column">
        <img alt="<%= produto.getNome()%>" src="<%= produto.getImagem()%>">
    </div>

    <!-- Coluna da direita -->
    <div class="right-column">

        <!-- Descrição do produto -->
        <div class="product-description">
            <span><%= produto.getCategory()%></span>
            <h1><%= produto.getNome()%></h1>
            <p><%= produto.getDescricao()%></p>
        </div>

        <!-- Preço do produto -->
        <div class="product-price">
            <span><%= new Helper().tryParseMoneyFormat(produto.getPreco())%></span>
            <form action="cart" method="post">
                <input type="hidden" name="addProductId" value="<%= produto.getId()%>">
                <button class="cart-btn" type="submit">+ Cart</button>
            </form>
            <!-- Botão de favorito -->
            <form action="product" method="post">
                <input type="hidden" name="productId" value="<%= produto.getId()%>">
                <input type="hidden" name="fav" value="<%= produto.getId()%>">
                <input <%= (produto.getFavoritoDoUser() ? "checked" : "")%> onchange="this.form.submit()" id="toggle-heart" type="checkbox" />
                <label for="toggle-heart">&#x2764;</label>
            </form>
        </div>
        <!-- Avaliação do produto (em estrelas) -->
        <div class="rate" <%= (produto.getReviewDadaPeloUser() != null ? "style='pointer-events:none'" : "")%>>
            <input <%= (String.valueOf(produto.getReviewDadaPeloUser()).equals("5") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= produto.getId()%>&rating=5'" type="radio" id="star5" name="rate" value="5" />
            <label for="star5" title="text"></label>
            <input <%= (String.valueOf(produto.getReviewDadaPeloUser()).equals("4") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= produto.getId()%>&rating=4'" type="radio" id="star4" name="rate" value="4" />
            <label for="star4" title="text"></label>
            <input <%= (String.valueOf(produto.getReviewDadaPeloUser()).equals("3") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= produto.getId()%>&rating=3'" type="radio" id="star3" name="rate" value="3" />
            <label for="star3" title="text"></label>
            <input <%= (String.valueOf(produto.getReviewDadaPeloUser()).equals("2") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= produto.getId()%>&rating=2'" type="radio" id="star2" name="rate" value="2" />
            <label for="star2" title="text"></label>
            <input <%= (String.valueOf(produto.getReviewDadaPeloUser()).equals("1") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= produto.getId()%>&rating=1'" type="radio" id="star1" name="rate" value="1" />
            <label for="star1" title="text"></label>
        </div>
    </div>

</main>

<div class="product-rating">

    <!-- Coluna da esquerda (resumo da avaliação) -->
    <div class="left-rating">
        <div class="review-resume">Resumo das avaliações</div>
        <div class="review-rate"><%= produto.getResumoAvaliacoes()%> / 5</div>
        <div class="review-list">
            <ul>
                <li>
                    <div>5 ESTRELAS</div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= produto.getBarra5Estrelas()%>%;"></div>
                    </div>
                    <div class="count"><%= produto.getQuantidadeAvaliacoesNota5()%></div>
                </li>
                <li>
                    <div>4 ESTRELAS</div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= produto.getBarra4Estrelas()%>%;"></div>
                    </div>
                    <div class="count"><%= produto.getQuantidadeAvaliacoesNota4()%></div>
                </li>
                <li>
                    <div>3 ESTRELAS</div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= produto.getBarra3Estrelas()%>%;"></div>
                    </div>
                    <div class="count"><%= produto.getQuantidadeAvaliacoesNota3()%></div>
                </li>
                <li>
                    <div>2 ESTRELAS</div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= produto.getBarra2Estrelas()%>%;"></div>
                    </div>
                    <div class="count"><%= produto.getQuantidadeAvaliacoesNota2()%></div>
                </li>
                <li>
                    <div>1 ESTRELAS</div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= produto.getBarra1Estrelas()%>%;"></div>
                    </div>
                    <div class="count"><%= produto.getQuantidadeAvaliacoesNota1()%></div>
                </li>
            </ul>
        </div>
    </div>

    <!-- Coluna da direita (avaliações em si) -->
    <div class="right-rating">

        <%
            for (ProductReviewListDTO avaliacao : avaliacoes) {
        %>
        <!-- Avaliação do cliente -->
        <div class="review-star">
            <%
                for (int r = 0; r < Integer.valueOf(avaliacao.getReview()); r++) {
            %>
            &#x2605;
            <%
                }
            %>
            <div class="review-date"><%= avaliacao.getReviewDataSimples()%></div>
        </div>
        <div class="review-title"><%= avaliacao.getReviewTitulo()%><div class="review-text"><%= avaliacao.getReviewDescricao()%></div>
        </div>
        <div class="review-name"><%= avaliacao.getAvaliador()%></div>
        <%
            }
        %>

        <% if (produto.getReviewDadaPeloUser() == null) {%>
        <!-- Botão para avaliar o produto -->
        <div class="review-btn">
            <a href="reviews?productId=<%= produto.getId()%>">Adicionar uma avaliação</a>
        </div>
        <% }%>
    </div>
</div>

<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>
