<%-- 
    Document   : header
    Created on : 12/09/2019, 19:35:15
    Author     : Caio
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<fmt:setLocale value="${sessionScope.appLocale}"/>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html>
<html lang="${empty sessionScope.appLocale ? 'en' : sessionScope.appLocale.toLanguageTag()}">
    <head>
        <title>UFF | ${param.title}</title>
        <meta charset="UTF-8">
        <!-- tirando cache p obrigar a pag a recarregar -->
        <meta http-equiv="cache-control" content="no-cache">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="css/style.css" type="text/css" rel="stylesheet">
        <link href="css/produto.css" type="text/css" rel="stylesheet">
        <link href="css/login.css" type="text/css" rel="stylesheet">
        <link href="css/carrinho.css" type="text/css" rel="stylesheet">
        <link href="css/carrinho-confirma.css" type="text/css" rel="stylesheet">
        <link href="css/produtos.css" type="text/css" rel="stylesheet">
        <link href="css/home.css" type="text/css" rel="stylesheet">
        <link href="css/minha-conta.css" type="text/css" rel="stylesheet">
        <script src="https://kit.fontawesome.com/24de11fb10.js" crossorigin="anonymous"></script>
    </head>

    <body>
        <fmt:message key="navigation.account" var="accountLabel"/>
        <fmt:message key="navigation.cart" var="cartLabel"/>
        <fmt:message key="navigation.search" var="searchLabel"/>
        <%
            // mostra se tiver msg
            if (session.getAttribute("msg") != null) {
                String msg = session.getAttribute("msg").toString();
                session.setAttribute("msg", null);
                out.println("<div id='msg-alerta-pop-up' style='display: none;'>" + msg + "</div>");
                out.println("<script>alert('" + msg + "');</script>");
            }
        %>
        <header>
            <!-- Logo -->
            <div class="logo-div">
                <a href="./">UFF</a>
            </div>
            <!-- Dropdown -->
            <div class="dropdown-menu">
                <div class="dropdown ps">
                    <button class="dropbtn" onclick="location.href = 'produtos?categoryId=1';">Playstation</button>
                    <div class="dropdown-content">
                        <a href="produtos?categoryId=5">Consoles</a>
                        <a href="produtos?categoryId=6">Jogos</a>
                        <a href="produtos?categoryId=4">Acessórios</a>
                    </div>
                </div>
                <div class="dropdown">
                    <button class="dropbtn" onclick="location.href = 'produtos?categoryId=2';">Xbox</button>
                    <div id="feminino" class="dropdown-content">
                        <a href="produtos?categoryId=8">Consoles</a>
                        <a href="produtos?categoryId=9">Jogos</a>
                        <a href="produtos?categoryId=7">Acessórios</a>
                    </div>
                </div>
                <div class="dropdown wii">
                    <button class="dropbtn" onclick="location.href = 'produtos?categoryId=3';">Wii</button>
                    <div id="infantil" class="dropdown-content">
                        <a href="produtos?categoryId=11">Consoles</a>
                        <a href="produtos?categoryId=12">Jogos</a>
                        <a href="produtos?categoryId=10">Acessórios</a>
                    </div>
                </div>
            </div>
            <div class="canto-header">
                <!-- Pesquisa -->
                <div class="consulta-header">
                    <form id="pesquisa" method="POST" action="produtos">
                        <input name="pesquisa" type="search" placeholder="${searchLabel}" required>
                    </form>
                </div>
                <!-- Minha Conta -->
                <div class="login-header">
                    <a href="usuario"><img alt="${accountLabel}" src="img/my-account.png" width="20" style="vertical-align: middle"></a>
                </div>
                <!-- Carrinho -->
                <div class="carrinho-header">
                    <a href="carrinho"><img alt="${cartLabel}" src="img/cart.png" width="25" style="vertical-align: middle"></a>
                </div>
                <form method="post" action="language" class="language-switcher">
                    <label for="locale"><fmt:message key="language.label"/></label>
                    <select id="locale" name="locale" onchange="this.form.submit()">
                        <option value="en" ${sessionScope.appLocale.language == 'en' ? 'selected' : ''}><fmt:message key="language.english"/></option>
                        <option value="pt-BR" ${sessionScope.appLocale.toLanguageTag() == 'pt-BR' ? 'selected' : ''}><fmt:message key="language.brazilianPortuguese"/></option>
                        <option value="es" ${sessionScope.appLocale.language == 'es' ? 'selected' : ''}><fmt:message key="language.spanish"/></option>
                        <option value="pt-PT" ${sessionScope.appLocale.toLanguageTag() == 'pt-PT' ? 'selected' : ''}><fmt:message key="language.portuguese"/></option>
                    </select>
                </form>
            </div>
        </header>
        <!-- Pra impedir de ficar elementos em cima do header -->
        <div class="fundo-branco"></div>
