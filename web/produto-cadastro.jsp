<%-- 
    Document   : produto-cadastro
    Created on : 02/10/2019, 02:09:04
    Author     : HP
--%>
<%@page import="br.uff.models.Product"%>
<%@page import="br.uff.dao.Components"%>
<%
    // se n usuario n for adm retorna p ProdutosController
    if (!session.getAttribute("userRole").equals("1")) {
        response.sendRedirect("UserController?redirect=ProdutosController");
    }

    if (session.getAttribute("msg") != null) {
        String msg = session.getAttribute("msg").toString();
        session.setAttribute("msg", null);
        out.println("<script>alert('" + msg + "');</script>");
    }

    Product produto = null;
    if (request.getAttribute("produto") != null) {
        produto = (Product) request.getAttribute("produto");
    } else {
        response.sendRedirect("ProdutoAdmController");
        return;
    }
%>
<!-- Header -->
<jsp:include page="header.jsp">
    <jsp:param name="title" value="Cadastro de Produtos"/>
</jsp:include>
<form method="post" action="ProdutoAdmController">
    <button name="action" formnovalidate value="unsel">Voltar</button>
    <fieldset>
        <legend>product</legend>
        <input value="${produto.getName()}" name="name" required type="text" placeholder="name" maxlength="255"/>
        <input value="${produto.getPrice()}" name="price" required type="number" min="0.01" step="0.01" placeholder="price" maxlength="255" />
        <input value="${produto.getDescription()}" name="description" required type="text" placeholder="description" maxlength="255" />
        <jsp:include page="partials/components/select.jsp">
            <jsp:param name="nameSelect" value="categoryId"/>
            <jsp:param name="required" value="1"/>
            <jsp:param name="consulta" value="SELECT id value,category_name text FROM vw_category ORDER BY 2"/>
            <jsp:param name="selectedValue" value="${produto.getCategoryId()}"/>
        </jsp:include>
        <input name="img" id="img" accept="image/*" <% if (request.getParameter("sel").equals("")) { %>required<% }%> type="file"/>
        <input value="${produto.getImg()}" style="display:none;" name="src" readonly type="text"/>
        <input name="quantity" required readonly type="text" value="${produto.getQuantity()}"/>
        <% if (!request.getParameter("sel").equals("")) { %>
        <button type="submit" name="action" value="del" formnovalidate onclick="return confirm('Tem certeza que deseja excluir esse produto?');false;">Apagar</button>
        <% }%>
        <button type="submit" name="action" value="grava">Gravar</button>
    </fieldset>
</form>
<fieldset>
    <legend>Previa da Imagem</legend>
    <img style="width: 100px;height:auto" id="prev"/>
</fieldset>
<% if (!request.getParameter("sel").equals("")) { %>
<form action="ProdutoAdmController" method="post">
    <fieldset>
        <legend>Incrementar Unidades do Produto no Estoque</legend>
        <input name="quantity" min="1" required type="number" placeholder="Quantidade a entrar no estoque" maxlength="11">
        <button type="submit" name="action" value="estoqueInsere">Inserir</button>
    </fieldset>
</form>
<% }%>
<script>
    window.onload = function () {
        // qnd a pag esta carregada set prev
        setPrevBySrcInput();
    }

    const toBase64 = file => new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => resolve(reader.result);
            reader.onerror = error => reject(error);
        });

    async function fileToBase64(seletor) {
        const file = document.querySelector(seletor).files[0];
        const src = await toBase64(file);
        return src;
    }

    document.querySelector('#img').addEventListener('change', function () {
        var maxAllowedSize = 1 * 1024 * 1024;
        if (this.files[0].size > maxAllowedSize) {
            alert("Arquivo grande demais, selecione outro menor!");
            this.value = "";
        } else {
            atualizaImg();
        }

    }, false);

    async function atualizaImg() {
        const src = await fileToBase64('#img');
        setValue('[name=src]', src);
        setPrevBySrcInput();
    }

    function setValue(seletor, value) {
        document.querySelector(seletor).value = String(value);
    }

    function setPrevBySrcInput() {
        document.querySelector('#prev').src = document.querySelector('[name=src]').value;
    }



</script>
<!-- Footer -->
<jsp:include page="footer.jsp"></jsp:include>