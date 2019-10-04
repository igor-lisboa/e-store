<%-- 
    Document   : produto-avalia
    Created on : 02/10/2019, 23:33:28
    Author     : HP
--%>
<%
    // se n tiver um usuario logado retorna p controller
    if (session.getAttribute("userId") == null) {
        response.sendRedirect("UserController?redirect=ProdutoController");
    }
    // se n tiver produto selecionado retorna p pag d produtos
    if (request.getAttribute("produtoId") == null && session.getAttribute("produtoId") == null && request.getParameter("produtoId") == null) {
        response.sendRedirect("ProdutosController");
    }
    // mostra msg se tiver
    if (request.getAttribute("msg") != null) {
        out.println("<script>alert('" + request.getAttribute("msg") + "');</script>");
    }
    // define rating
    String rating = "";
    if (request.getParameter("rating") != null) {
        rating = request.getParameter("rating");
    }
%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Avalia Produto"/>
</jsp:include>
<a href="ProdutoController?produtoId=<%= session.getAttribute("produtoId")%>">Voltar</a>
<form action="AvaliacaoController?produtoId=2" method="post">
    <fieldset>
        <legend>Avalia��o</legend>
        <!-- Avalia��o do produto (em estrelas) -->
        <div class="rate">
            <input <%= rating.equals("5") ? "selected" : ""%> type="radio" id="star5" name="rating" value="5" />
            <label for="star5" title="text"></label>
            <input <%= rating.equals("4") ? "selected" : ""%> type="radio" id="star4" name="rating" value="4" />
            <label for="star4" title="text"></label>
            <input <%= rating.equals("3") ? "selected" : ""%> type="radio" id="star3" name="rating" value="3" />
            <label for="star3" title="text"></label>
            <input <%= rating.equals("2") ? "selected" : ""%> type="radio" id="star2" name="rating" value="2" />
            <label for="star2" title="text"></label>
            <input <%= rating.equals("1") ? "selected" : ""%> type="radio" id="star1" name="rating" value="1" />
            <label for="star1" title="text"></label>
        </div>
        <input name="title" required type="text" placeholder="T�tulo da Avalia��o" maxlength="255" />
        <input name="description" required type="text" placeholder="Descricao da Avalia��o" maxlength="255" />
        <button name="action" value="avalia" type="submit">Salvar</button>
    </fieldset>
</form>
<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>