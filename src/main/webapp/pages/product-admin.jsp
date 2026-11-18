<%-- 
    Document   : produto-grid
    Created on : 02/10/2019, 02:09:29
    Author     : HP
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="br.uff.store.infrastructure.shared.Helper"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.ProductDTO"%>
<%@page import="br.uff.store.core.enums.UserRole"%>
<%@page import="java.util.ArrayList"%>
<%
    // se n usuario n for adm retorna p ProductsController
    if (!session.getAttribute("userRole").equals(UserRole.ADM.getId())) {
        response.sendRedirect("account");
    }
    List<ProductDTO> grid = new ArrayList<ProductDTO>();
    if (request.getAttribute("grid") != null) {
        grid = (List<ProductDTO>) request.getAttribute("grid");
    }
%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Grid de Products"/>
</jsp:include>

<table class="product-grid">
    <caption>Products</caption>
    <thead>
        <tr>
            <th scope="row" colspan="5"><h2>Products</h2></th>
        </tr>
        <tr class="product-grid-title">
            <td>Operações</td>
            <td>Nome</td>
            <td>Preço</td>
            <td>Category</td>
            <td>Quantidade no Estoque</td>
        </tr>
    </thead>
    <tbody>
        <%
            for (ProductDTO produto : grid) {
        %>
        <tr class="product-data">
            <td>
                <a href="product-admin?selectedId=<%= produto.getId()%>">Editar</a>             
            </td>
            <td><%= produto.getNome()%></td>
            <td><%= new Helper().tryParseMoneyFormat(produto.getPreco())%></td>
            <td><%= produto.getCategory()%></td>
            <td><%= produto.getQuantidade()%></td>
        </tr>
        <%
            }
        %>
    </tbody>
    <tfoot>
        <tr class="product-data-add">
            <td colspan="5">
                <a class="end-link" href="product-admin?selectedId">Inserir Novo Product</a>
            </td>
        </tr>
    </tfoot>
</table>

<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>