<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.core.dtos.AddressDTO"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%

    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account?redirect=addresses");
    }

    List<AddressDTO> grid = new ArrayList<AddressDTO>();
    if (request.getAttribute("grid") != null) {
        grid = (List<AddressDTO>) request.getAttribute("grid");
    }
%>

<jsp:include page="header.jsp">
    <jsp:param name="title" value="Addresses"/>
</jsp:include>
<table class="end-grid">
    <caption><fmt:message key="ui.addresses"/></caption>
    <thead>
        <tr>
            <th scope="row" colspan="5"><h2><fmt:message key="ui.myAddresses"/></h2></th>
        </tr>
        <tr class="end-title-container">
            <td class="end-title"><fmt:message key="ui.operations"/></td>
            <td class="end-title"><fmt:message key="ui.addressDescription"/></td>
            <td class="end-title"><fmt:message key="ui.address"/></td>
            <td class="end-title"><fmt:message key="ui.city"/></td>
            <td class="end-title"><fmt:message key="ui.state"/></td>
        </tr>
    </thead>
    <tbody>
        <%
            for (AddressDTO address : grid) {
        %>
        <tr class="end-data-container">
            <td class="end-data">
                <a class="end-link" href="addresses?selectedId=<%= address.getId()%>"><fmt:message key="ui.edit"/></a>
            </td>
            <td class="end-data"><%= Html.escape(address.getName()) %></td>
            <td class="end-data"><%= Html.escape(address.getStreetAddress()) %></td>
            <td class="end-data"><%= Html.escape(address.getCity()) %></td>
            <td class="end-data"><%= Html.escape(address.getState()) %></td>
        </tr>
        <%
            }
        %>
    </tbody>
    <tfoot>
        <tr>
            <td class="end-add-new" colspan="5">
                <a class="end-link" href="addresses?selectedId"><fmt:message key="ui.newAddress"/></a>
            </td>
        </tr>
    </tfoot>
</table>

<jsp:include page="footer.jsp"></jsp:include>
