<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="br.uff.store.core.dtos.CategoryDTO"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.ProductDTO"%>
<%@page import="br.uff.store.core.enums.UserRole"%>
<%

    if (!session.getAttribute("userRole").equals(UserRole.ADMIN.getId())) {
        response.sendRedirect("account?redirect=products");
    }

    ProductDTO product = new ProductDTO();
    if (request.getAttribute("product") != null) {
        product = (ProductDTO) request.getAttribute("product");
    } else {
        response.sendRedirect("product-admin");
        return;
    }

    List<CategoryDTO> categories = new ArrayList<CategoryDTO>();
    if (request.getAttribute("categories") != null) {
        categories = (List<CategoryDTO>) request.getAttribute("categories");
    }

    String sel = request.getParameter("selectedId");
%>

<fmt:message key="ui.pageTitle.productDetails" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>
<fmt:message key="ui.js.confirmDeleteProduct" var="js_confirmDeleteProduct"/>
<fmt:message key="ui.js.imageTooLarge" var="js_imageTooLarge"/>

<div class="product-main-container">

    <div class="product-left-container">

        <h2><fmt:message key="ui.product"/></h2>
        <form method="post" action="product-admin">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

            <ul class="form-style-1">
                <li>
                    <label for="field-input0"><fmt:message key="ui.name"/></label>
                    <input id="field-input0" value="<%= Html.escape(product.getName()) %>" class="field-long" name="name" required type="text" maxlength="255" />
                </li>
                <li>
                    <label for="field-input1"><fmt:message key="ui.price"/></label>
                    <input id="field-input1" value="<%= product.getPrice()%>" class="field-long" name="price" required type="number" max="5000" min="0.01" step="0.01" maxlength="255" />
                </li>
                <li>
                    <label for="field-description"><fmt:message key="ui.description"/></label>
                    <textarea id="field-description" class="field-long field-textarea" name="description" required maxlength="255"><%= Html.escape(product.getDescription()) %></textarea>
                </li>
                <li>
                    <label for="field-categoryId"><fmt:message key="ui.category"/></label>
                    <select id="field-categoryId" required name="categoryId">
                        <% for (CategoryDTO category : categories) {%>
                        <option <%= (product.getCategoryId() == category.getId() ? "selected" : "")%> value="<%= category.getId()%>"><%= Html.escape(category.getName()) %></option>
                        <% } %>
                    </select>
                </li>
                <li>
                    <label for="img"><fmt:message key="ui.image"/></label><input name="img" id="img" accept="image/*" <% if (sel.equals("")) { %>required<% }%> type="file"/>
                    <input value="<%= Html.escape(product.getImage()) %>" style="display:none;" name="src" readonly type="text"/>
                </li>
                <li class="center">
                    <button name="action" formnovalidate value="clear"><fmt:message key="ui.back"/></button>
                    <button type="submit" name="action" value="save"><fmt:message key="ui.save"/></button>
                    <% if (!sel.equals("")) { %>
                    <button type="submit" name="action" value="delete" formnovalidate onclick="return confirm('${js_confirmDeleteProduct}');false;"><fmt:message key="ui.delete"/></button>
                    <% }%>
                </li>
            </ul>
        </form>
    </div>

    <div class="product-right-container">

        <h2><fmt:message key="ui.imagePreview"/></h2>
        <div class="preview-image">
            <img alt="Product preview" id="prev"/>
        </div>

        <% if (!sel.equals("")) { %>
        <form action="product-admin" method="post">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
            <ul class="form-style-1">
                <li>
                    <label for="field-quantity"><fmt:message key="ui.stockIncrement"/></label>
                    <input id="field-quantity" name="quantity" min="1" required type="number" maxlength="11" class="field-divided">
                </li>
                <li class="center">
                    <button type="submit" name="action" value="addStock"><fmt:message key="ui.insert"/></button>
                </li>
            </ul>
        </form>
        <% }%>
    </div>
</div>
<script>
    window.onload = function () {

        setPrevBySrcInput();
    }

    const toBase64 = file => new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result);
            reader.onerror = error => reject(error);
        });

    async function fileToBase64(selector) {
        const file = document.querySelector(selector).files[0];
        const src = await toBase64(file);
        return src;
    }

    document.querySelector('#img').addEventListener('change', function () {
        var maxAllowedSize = 1 * 1024 * 1024;
        if (this.files[0].size > maxAllowedSize) {
            alert("${js_imageTooLarge}");
            this.value = "";
        } else {
            updateImagePreview();
        }

    }, false);

    async function updateImagePreview() {
        const src = await fileToBase64('#img');
        setValue('[name=src]', src);
        setPrevBySrcInput();
    }

    function setValue(selector, value) {
        document.querySelector(selector).value = String(value);
    }

    function setPrevBySrcInput() {
        document.querySelector('#prev').src = document.querySelector('[name=src]').value;
    }
</script>

<jsp:include page="footer.jsp"></jsp:include>
