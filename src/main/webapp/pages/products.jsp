<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="br.uff.store.core.dtos.ProductListDTO"%>
<%@page import="br.uff.store.core.dtos.PaginationDTO"%>
<%@page import="java.util.List"%>
<fmt:message key="ui.pageTitle.products" var="pageTitle"/>
<fmt:message key="ui.searchPlaceholder" var="searchPlaceholder"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>
<fmt:message key="ui.js.priceRange" var="js_priceRange"/>
<%
    PaginationDTO<List<ProductListDTO>> products = new PaginationDTO<List<ProductListDTO>>();
    if (request.getAttribute("products") != null) {
        products = (PaginationDTO<List<ProductListDTO>>) request.getAttribute("products");
    }
%>
<form method="post" action="products" id="frmProducts">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

    <div class="products-main">


        <div class="products-filter">

            <div class="products-item-filter">
                <p><fmt:message key="ui.searchLabel"/></p>
                <input name="search" aria-label="${searchPlaceholder}" value="${fn:escapeXml(search)}" type="search" placeholder="${searchPlaceholder}" onchange="document.getElementById('frmProducts').submit()">
            </div>
            <div class="products-item-filter">
                <p><fmt:message key="ui.categories"/></p>
                <input ${playstation} onchange="document.getElementById('frmProducts').submit()" name="category" id="playstation" type="checkbox" value="playstation"><label for="playstation">Playstation</label><br>
                <input ${xbox} onchange="document.getElementById('frmProducts').submit()" name="category" id="xbox" type="checkbox" value="xbox"><label for="xbox">Xbox</label><br>
                <input ${nintendo} onchange="document.getElementById('frmProducts').submit()" name="category" id="nintendo" type="checkbox" value="nintendo"><label for="nintendo">Nintendo</label><br>
            </div>

            <div class="products-item-filter">
                <p><fmt:message key="ui.subcategories"/></p>
                <input ${consoles} onchange="document.getElementById('frmProducts').submit()" name="subCategory" type="checkbox" value="consoles" id="consoles"><label for="consoles"><fmt:message key="ui.consoles"/></label><br>
                <input ${games} onchange="document.getElementById('frmProducts').submit()" name="subCategory" type="checkbox" value="games" id="games"><label for="games"><fmt:message key="ui.games"/></label><br>
                <input ${accessories} onchange="document.getElementById('frmProducts').submit()" name="subCategory" type="checkbox" id="accessories" value="accessories"><label for="accessories"><fmt:message key="ui.accessories"/></label><br>
            </div>

            <div class="products-item-filter">
                <p><fmt:message key="ui.prices"/></p>
                <label id="minPriceLabel" for="display_min"><fmt:message key="ui.minPrice"/></label>
                <div class="products-range">
                    <input aria-labelledby="minPriceLabel" value="${priceMin}" onchange="validatePrices()" type="range" min="0" max="5000" step="100" oninput="display_min.value=value" onchange="display_min.value = value">
                    <input value="${priceMin}" name="price_min" type="number" readonly id="display_min"/>
                </div>
                <label id="maxPriceLabel" for="display_max"><fmt:message key="ui.maxPrice"/></label>
                <div  class="products-range">
                    <input aria-labelledby="maxPriceLabel" value="${priceMax}" onchange="validatePrices()" type="range" min="0" max="5000" step="100" oninput="display_max.value=value" onchange="display_max.value = value">
                    <input value="${priceMax}" name="price_max" type="number" readonly id="display_max"/>
                </div>
            </div>

            <div class="products-item-filter">
                <input ${favorites} onchange="document.getElementById('frmProducts').submit()" name="special" type="checkbox" value="favorites" id="favs"><label for="favs"><fmt:message key="ui.favoritesOnly"/></label><br>
                <input ${newReleases} onchange="document.getElementById('frmProducts').submit()" name="special" type="checkbox" value="newReleases" id="newReleasesFilter"><label for="newReleasesFilter"><fmt:message key="ui.newOnly"/></label><br>
            </div>
        </div>


        <div class="products-page">
            <% if (products.getItems().size() < 1) { %>
            <h3><fmt:message key="ui.emptyProducts"/></h3>
            <%
            } else {
            %>
            <div class="products-container">
                <%
                    for (ProductListDTO product : products.getItems()) {
                %>
                <div class="products-item">
                    <a href="product?productId=<%= product.getId()%>">
                        <div class="products-cart"><fmt:message key="ui.viewDetails"/></div>
                        <img alt="<%= Html.escape(product.getName()) %>" src="<%= Html.escape(product.getImage()) %>">
                        <div class="products-title"><%= Html.escape(product.getName()) %></div>
                        <div class="products-details"><%= Html.escape(product.getCategory()) %></div>
                        <div class="products-price"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(product.getPrice())%></div>
                        <% if (!product.isAvailable()) { %>
                        <div class="products-unavailable"><fmt:message key="ui.outOfStock"/></div>
                        <% } %>
                    </a>
                </div>
                <%
                    }
                %>
            </div>
            <%
                }
            %>

            <div class="products-paging">
                <% if (products.getPageNumber() > 1) {%>
                <button class="products-prev" name="action" value="previous">&#8249;</button>
                <% }%>
                <span><%= products.getPageNumber()%></span>
                <% if (products.getPageNumber() < products.getLastPage()) { %>
                <button class="products-next" name="action" value="next">&#8250;</button>
                <% }%>
            </div>
        </div>
    </div>
</form>
<script>
    function validatePrices() {
        var price_min = document.getElementById('display_min').value;
        var price_max = document.getElementById('display_max').value;
        if (parseInt(price_min) >= parseInt(price_max)) {
            alert("${js_priceRange}");
            return false;
        } else {
            document.getElementById('frmProducts').submit();
            return true;
        }
    }
</script>

<jsp:include page="footer.jsp"></jsp:include>
