<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="br.uff.store.application.security.Html"%>
<%@page import="br.uff.store.core.dtos.ProductReviewInputDTO"%>
<%

    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account?redirect=reviews");
    }

    if (session.getAttribute("productId") == null) {
        response.sendRedirect("products");
    }

    Integer rating = 0;
    if (session.getAttribute("rating") != null) {
        rating = (new Helper()).tryParseInteger(session.getAttribute("rating").toString());
    }

    ProductReviewInputDTO existing = (ProductReviewInputDTO) request.getAttribute("review");
    boolean editing = existing != null;
    if (editing) {
        rating = existing.getRating();
    }
%>

<fmt:message key="ui.pageTitle.productReview" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>

<form action="reviews" method="post">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

    <ul class="form-style-1">

        <li>
            <label id="ratingLabel"><fmt:message key="ui.rating"/></label>
            <div class="rate" role="radiogroup" aria-labelledby="ratingLabel">
                <input <%= rating.equals(5) ? "checked" : ""%> type="radio" id="star5" name="rating" value="5" />
                <label for="star5" title="text"></label>
                <input <%= rating.equals(4) ? "checked" : ""%> type="radio" id="star4" name="rating" value="4" />
                <label for="star4" title="text"></label>
                <input <%= rating.equals(3) ? "checked" : ""%> type="radio" id="star3" name="rating" value="3" />
                <label for="star3" title="text"></label>
                <input <%= rating.equals(2) ? "checked" : ""%> type="radio" id="star2" name="rating" value="2" />
                <label for="star2" title="text"></label>
                <input <%= rating.equals(1) ? "checked" : ""%> type="radio" id="star1" name="rating" value="1" />
                <label for="star1" title="text"></label>
            </div>
            <br>
        </li>
        <li>
            <br>
            <label for="field-title"><fmt:message key="ui.title"/></label>
            <input id="field-title" class="field-long" name="title" required type="text" maxlength="255" value="<%= editing ? Html.escape(existing.getTitle()) : "" %>" />
        </li>
        <li>
            <label for="field-description"><fmt:message key="ui.description"/></label>
            <textarea id="field-description" class="field-long field-textarea" name="description" required type="text" maxlength="255"><%= editing && existing.getDescription() != null ? Html.escape(existing.getDescription()) : "" %></textarea>
        </li>
        <li class="center">
            <a href="product"><fmt:message key="ui.back"/></a>
            <button name="action" value="<%= editing ? "updateReview" : "submitReview" %>" type="submit"><fmt:message key="ui.saveAction"/></button>
        </li>
    </ul>
</form>

<% if (editing) { %>
<form action="reviews" method="post" class="center" onsubmit="return confirm('<fmt:message key="ui.confirmDeleteReview"/>');">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
    <button name="action" value="deleteReview" type="submit"><fmt:message key="ui.deleteReview"/></button>
</form>
<% } %>


<jsp:include page="footer.jsp"></jsp:include>
