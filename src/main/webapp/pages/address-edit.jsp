<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.core.dtos.AddressDTO"%>
<%

    if (session.getAttribute("userId") == null) {
        response.sendRedirect("account?redirect=addresses");
    }

    AddressDTO address = new AddressDTO();
    if (request.getAttribute("addresses") != null) {
        address = (AddressDTO) request.getAttribute("addresses");
    } else {
        response.sendRedirect("addresses");
        return;
    }

    String sel = request.getParameter("selectedId");
%>

<jsp:include page="header.jsp">
    <jsp:param name="title" value="Address details"/>
</jsp:include>

<div class="end-add-container">
    <form action="addresses" method="post">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

        <div class="end-group-container">
            <div class="group">
                <input value="<%= Html.escape(address.getName()) %>" name="name" required type="text" maxlength="255" />
                <span class="highlight"></span>
                <span class="bar"></span>
                <label><fmt:message key="ui.addressName"/></label>
            </div>

            <div class="group">
                <input value="<%= address.getPostalCode()%>" name="zipcode" required type="number" maxlength="11" id="postalCode" onblur="lookupPostalCode(this.value);"/>
                <span class="highlight"></span>
                <span class="bar"></span>
                <label><fmt:message key="ui.postalCode"/></label>
            </div>

            <div class="group">
                <input value="<%= Html.escape(address.getStreetAddress()) %>" name="address" required type="text" id="street" maxlength="255" />
                <span class="highlight"></span>
                <span class="bar"></span>
                <label><fmt:message key="ui.streetAddress"/></label>
            </div>

            <div class="group">
                <input value="<%= Html.escape(address.getCity()) %>" name="city" required type="text" id="city"  maxlength="255" />
                <span class="highlight"></span>
                <span class="bar"></span>
                <label><fmt:message key="ui.city"/></label>
            </div>

            <div class="group">
                <input value="<%= Html.escape(address.getState()) %>" name="state" required type="text" id="uf" maxlength="255" />
                <span class="highlight"></span>
                <span class="bar"></span>
                <label><fmt:message key="ui.state"/></label>
            </div>

            <div class="group">
                <input name="country" required type="text" readonly value="<%= Html.escape(address.getCountry()) %>" maxlength="255" />
                <span class="highlight"></span>
                <span class="bar"></span>
                <label></label>
            </div>
        </div>

        <button class="standard-btn" name="action" formnovalidate value="clear"><fmt:message key="ui.back"/></button>
        <button class="standard-btn" name="action" value="save" type="submit"><fmt:message key="ui.saveAction"/></button>
        <% if (!sel.equals("")) { %>
        <button class="standard-btn" type="submit" name="action" value="delete" formnovalidate onclick="return confirm('Delete this address?');false;"><fmt:message key="ui.delete"/></button>
        <% }%>
    </form>
</div>
<script type="text/javascript" >

    function clearPostalAddress() {

        document.getElementById('street').value = ("");
        document.getElementById('city').value = ("");
        document.getElementById('uf').value = ("");
    }

    function fillPostalAddress(result) {
        if (!("erro" in result)) {

            document.getElementById('street').value = (result.logradouro);
            document.getElementById('city').value = (result.localidade);
            document.getElementById('uf').value = (result.uf);
        } //end if.
        else {

            clearPostalAddress();
            alert("Postal code not found.");
        }
    }

    function lookupPostalCode(value) {

        var postalCode = value.replace(/\D/g, '');

        if (postalCode != "") {

            var validPostalCode = /^[0-9]{8}$/;

            if (validPostalCode.test(postalCode)) {

                document.getElementById('street').value = "...";
                document.getElementById('city').value = "...";
                document.getElementById('uf').value = "...";

                var script = document.createElement('script');

                script.src = 'https://viacep.com.br/ws/' + postalCode + '/json/?callback=fillPostalAddress';

                document.body.appendChild(script);

            } //end if.
            else {

                clearPostalAddress();
                alert("Invalid Brazilian postal code.");
            }
        } //end if.
        else {

            clearPostalAddress();
        }
    }
    ;
</script>

<jsp:include page="footer.jsp"></jsp:include>
