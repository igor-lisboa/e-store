<%-- 
    Document   : produtos
    Created on : 02/10/2019, 00:11:02
    Author     : HP
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="br.uff.store.core.dtos.ProductListDTO"%>
<%@page import="br.uff.store.core.dtos.PaginateDTO"%>
<%@page import="java.util.List"%>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Products"/>
</jsp:include>
<%
    PaginateDTO<List<ProductListDTO>> produtos = new PaginateDTO<List<ProductListDTO>>();
    if (request.getAttribute("products") != null) {
        produtos = (PaginateDTO<List<ProductListDTO>>) request.getAttribute("products");
    }
%>
<form method="post" action="products" id="frmProducts">
    <!-- Container principal -->
    <div class="products-main">

        <!-- Container dos filtros -->
        <div class="products-filter">
            <!-- Filtro das categorias -->
            <div class="products-item-filter">
                <p>Nome ou Descrição</p>
                <input name="search" value="${search}" type="search" placeholder="Buscar..." onchange="document.getElementById('frmProducts').submit()">
            </div>
            <div class="products-item-filter">
                <p>Categorys</p>
                <input ${playstation} onchange="document.getElementById('frmProducts').submit()" name="category" id="playstation" type="checkbox" value="playstation"><label for="playstation">Playstation</label><br>
                <input ${xbox} onchange="document.getElementById('frmProducts').submit()" name="category" id="xbox" type="checkbox" value="xbox"><label for="xbox">Xbox</label><br>
                <input ${wii} onchange="document.getElementById('frmProducts').submit()" name="category" id="wii" type="checkbox" value="wii"><label for="wii">Wii</label><br>
            </div>
            <!-- Fitro das subcategorias -->
            <div class="products-item-filter">
                <p>Subcategorias</p>
                <input ${consoles} onchange="document.getElementById('frmProducts').submit()" name="subCategory" type="checkbox" value="consoles" id="consoles"><label for="consoles">Consoles</label><br>
                <input ${games} onchange="document.getElementById('frmProducts').submit()" name="subCategory" type="checkbox" value="games" id="games"><label for="games">Jogos</label><br>
                <input ${accessories} onchange="document.getElementById('frmProducts').submit()" name="subCategory" type="checkbox" id="accessories" value="accessories"><label for="accessories">Acessórios</label><br>
            </div>
            <!-- Filtro dos preços -->
            <div class="products-item-filter">
                <p>Preços</p>
                <label>Valor Mínimo (R$)</label>
                <div class="products-range">
                    <input value="${priceMin}" onchange="verifica_precos()" type="range" min="0" max="5000" step="100" oninput="display_min.value=value" onchange="display_min.value = value">
                    <input value="${priceMin}" name="price_min" type="number" readonly id="display_min"/>
                </div>
                <label>Valor Máximo (R$)</label>
                <div  class="products-range">
                    <input value="${priceMax}" onchange="verifica_precos()" type="range" min="0" max="5000" step="100" oninput="display_max.value=value" onchange="display_max.value = value">
                    <input value="${priceMax}" name="price_max" type="number" readonly id="display_max"/>
                </div>
            </div>
            <!-- Filtros Especiais -->
            <div class="products-item-filter">
                <input ${favorites} onchange="document.getElementById('frmProducts').submit()" name="special" type="checkbox" value="favorites" id="favs"><label for="favs">Apenas Favoritos</label><br>
                <input ${newReleases} onchange="document.getElementById('frmProducts').submit()" name="special" type="checkbox" value="newReleases" id="lancs"><label for="lancs">Apenas Lançamentos</label><br>
            </div>
        </div>

        <!-- Container dos produtos -->
        <div class="products-page">
            <% if (produtos.getDados().size() < 1) { %>
            <h3>O filtro escolhido não retornou nenhum produto. Mas não desista, tente com outro.</h3>
            <%
            } else {
            %>
            <div class="products-container">
                <%
                    for (ProductListDTO produto : produtos.getDados()) {
                %>
                <div class="products-item">
                    <a href="product?productId=<%= produto.getId()%>">
                        <div class="products-cart">Ver Detalhes</div>
                        <img alt="<%= produto.getNome()%>" src="<%= produto.getImagem()%>">
                        <div class="products-title"><%= produto.getNome()%></div>
                        <div class="products-details"><%= produto.getCategory()%></div>
                        <div class="products-price"><%= new Helper().tryParseMoneyFormat(produto.getPreco())%></div>
                    </a>
                </div>
                <%
                    }
                %>
            </div>
            <%
                }
            %>
            <!-- Páginas -->
            <div class="products-paging">
                <% if (produtos.getPaginaAtual() > 1) {%>
                <button class="products-prev" name="action" value="previous">&#8249;</button>
                <% }%>
                <span><%= produtos.getPaginaAtual()%></span>
                <% if (produtos.getPaginaAtual() < produtos.getUltimaPagina()) { %>
                <button class="products-next" name="action" value="next">&#8250;</button>
                <% }%>
            </div>
        </div>
    </div>
</form>
<script>
    function verifica_precos() {
        var price_min = document.getElementById('display_min').value;
        var price_max = document.getElementById('display_max').value;
        if (parseInt(price_min) >= parseInt(price_max)) {
            alert("Valor mínimo(R$" + price_min + ") não pode ser igual ou maior que o valor máximo(R$" + price_max + ")!");
            return false;
        } else {
            document.getElementById('frmProducts').submit();
            return true;
        }
    }
</script>
<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>