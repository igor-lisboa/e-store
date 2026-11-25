<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.ProductReviewListDTO"%>
<%@page import="br.uff.store.core.dtos.CustomerProductDTO"%>
<%@page import="java.util.ArrayList"%>
<%

    String productId = "";
    if (session.getAttribute("productId") != null) {
        productId = session.getAttribute("productId").toString();
    } else {
        response.sendRedirect("product");
    }

    CustomerProductDTO product = new CustomerProductDTO();
    List<ProductReviewListDTO> reviews = new ArrayList<ProductReviewListDTO>();
    if (request.getAttribute("product") != null) {
        product = (CustomerProductDTO) request.getAttribute("product");
        reviews = (List<ProductReviewListDTO>) request.getAttribute("reviews");
    } else {
        session.setAttribute("msg", "Select a product.");
        response.sendRedirect("products");
        return;
    }
%>

<jsp:include page="header.jsp">
    <jsp:param name="title" value="<%= product.getName()%>"/>
</jsp:include>

<main class="container">


    <div class="left-column">
        <img alt="<%= Html.escape(product.getName()) %>" src="<%= Html.escape(product.getImage()) %>">
    </div>


    <div class="right-column">


        <div class="product-description">
            <span><%= Html.escape(product.getCategory()) %></span>
            <h1><%= Html.escape(product.getName()) %></h1>
            <p><%= Html.escape(product.getDescription()) %></p>
        </div>


        <div class="product-price">
            <span><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).tryParseMoneyFormat(product.getPrice())%></span>
            <form action="cart" method="post">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                <input type="hidden" name="addProductId" value="<%= product.getId()%>">
                <% if (product.getQuantity() != null && product.getQuantity() > 0) { %>
                <button class="cart-btn" type="submit"><fmt:message key="ui.addToCart"/></button>
                <% } else { %>
                <button class="cart-btn" type="button" disabled><fmt:message key="ui.outOfStock"/></button>
                <% } %>
            </form>

            <form action="product" method="post">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                <input type="hidden" name="productId" value="<%= product.getId()%>">
                <input type="hidden" name="fav" value="<%= product.getId()%>">
                <input <%= (product.getFavorite() ? "checked" : "")%> onchange="this.form.submit()" id="toggle-heart" type="checkbox" />
                <label for="toggle-heart">&#x2764;</label>
            </form>
        </div>

        <div class="rate" <%= (product.getUserRating() != null ? "style='pointer-events:none'" : "")%>>
            <input <%= (String.valueOf(product.getUserRating()).equals("5") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= product.getId()%>&rating=5'" type="radio" id="star5" name="rate" value="5" />
            <label for="star5" title="text"></label>
            <input <%= (String.valueOf(product.getUserRating()).equals("4") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= product.getId()%>&rating=4'" type="radio" id="star4" name="rate" value="4" />
            <label for="star4" title="text"></label>
            <input <%= (String.valueOf(product.getUserRating()).equals("3") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= product.getId()%>&rating=3'" type="radio" id="star3" name="rate" value="3" />
            <label for="star3" title="text"></label>
            <input <%= (String.valueOf(product.getUserRating()).equals("2") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= product.getId()%>&rating=2'" type="radio" id="star2" name="rate" value="2" />
            <label for="star2" title="text"></label>
            <input <%= (String.valueOf(product.getUserRating()).equals("1") ? "checked" : "")%> onClick="window.location.href = 'reviews?productId=<%= product.getId()%>&rating=1'" type="radio" id="star1" name="rate" value="1" />
            <label for="star1" title="text"></label>
        </div>
    </div>

</main>

<div class="product-rating">


    <div class="left-rating">
        <div class="review-resume"><fmt:message key="ui.reviewSummary"/></div>
        <div class="review-rate"><%= product.getAverageRating()%> / 5</div>
        <div class="review-list">
            <ul>
                <li>
                    <div>5 <fmt:message key="ui.stars"/></div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= product.getRatingBar5()%>%;"></div>
                    </div>
                    <div class="count"><%= product.getRatingCount5()%></div>
                </li>
                <li>
                    <div>4 <fmt:message key="ui.stars"/></div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= product.getRatingBar4()%>%;"></div>
                    </div>
                    <div class="count"><%= product.getRatingCount4()%></div>
                </li>
                <li>
                    <div>3 <fmt:message key="ui.stars"/></div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= product.getRatingBar3()%>%;"></div>
                    </div>
                    <div class="count"><%= product.getRatingCount3()%></div>
                </li>
                <li>
                    <div>2 <fmt:message key="ui.stars"/></div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= product.getRatingBar2()%>%;"></div>
                    </div>
                    <div class="count"><%= product.getRatingCount2()%></div>
                </li>
                <li>
                    <div>1 <fmt:message key="ui.stars"/></div>
                    <div class="percentage">
                        <div class="percentage-full" style="width: <%= product.getRatingBar1()%>%;"></div>
                    </div>
                    <div class="count"><%= product.getRatingCount1()%></div>
                </li>
            </ul>
        </div>
    </div>


    <div class="right-rating">

        <%
            for (ProductReviewListDTO rating : reviews) {
        %>

        <div class="review-star">
            <%
                for (int r = 0; r < Integer.valueOf(rating.getRating()); r++) {
            %>
            &#x2605;
            <%
                }
            %>
            <div class="review-date"><%= new Helper((java.util.Locale) session.getAttribute("appLocale")).formatReviewDate(rating.getShortDate()) %></div>
        </div>
        <div class="review-title"><%= Html.escape(rating.getTitle()) %><div class="review-text"><%= Html.escape(rating.getDescription()) %></div>
        </div>
        <div class="review-name"><%= Html.escape(rating.getReviewer()) %></div>
        <%
            }
        %>

        <% if (product.getUserRating() == null) {%>

        <div class="review-btn">
            <a href="reviews?productId=<%= product.getId()%>"><fmt:message key="ui.addReview"/></a>
        </div>
        <% } else {%>

        <div class="review-btn">
            <a href="reviews?productId=<%= product.getId()%>&edit=1"><fmt:message key="ui.editReview"/></a>
        </div>
        <% }%>
    </div>
</div>


<jsp:include page="footer.jsp"></jsp:include>
