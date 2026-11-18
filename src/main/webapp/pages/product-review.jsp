<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
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
%>

<jsp:include page="header.jsp">
    <jsp:param name="title" value="Product review"/>
</jsp:include>

<form action="reviews" method="post">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

    <ul class="form-style-1">

        <li>
            <label><fmt:message key="ui.rating"/></label>
            <div class="rate">
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
            <label><fmt:message key="ui.title"/></label>
            <input class="field-long" name="title" required type="text" maxlength="255" />
        </li>
        <li>
            <label><fmt:message key="ui.description"/></label>
            <textarea class="field-long field-textarea" name="description" required type="text" maxlength="255"></textarea>
        </li>
        <li class="center">
            <a href="product"><fmt:message key="ui.back"/></a>
            <button name="action" value="submitReview" type="submit"><fmt:message key="ui.saveAction"/></button>
        </li>
    </ul>
</form>


<jsp:include page="footer.jsp"></jsp:include>
