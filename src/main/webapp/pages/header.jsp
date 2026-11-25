<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<fmt:setLocale value="${sessionScope.appLocale}"/>
<fmt:setBundle basename="messages"/>
<!DOCTYPE html>
<html lang="${empty sessionScope.appLocale ? 'en' : sessionScope.appLocale.toLanguageTag()}">
    <head>
        <title>e-store | <c:out value="${param.title}"/></title>
        <meta charset="UTF-8">
        <meta name="csrf-token" content="${sessionScope.csrfToken}">

        <meta http-equiv="cache-control" content="no-cache">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link href="css/style.css" type="text/css" rel="stylesheet">
        <link href="css/catalog-cards.css" type="text/css" rel="stylesheet">
        <link href="css/product.css" type="text/css" rel="stylesheet">
        <link href="css/login.css" type="text/css" rel="stylesheet">
        <link href="css/cart.css" type="text/css" rel="stylesheet">
        <link href="css/cart-confirmation.css" type="text/css" rel="stylesheet">
        <link href="css/products.css" type="text/css" rel="stylesheet">
        <link href="css/home.css" type="text/css" rel="stylesheet">
        <link href="css/account.css" type="text/css" rel="stylesheet">
        <link href="css/account-forms.css" type="text/css" rel="stylesheet">
        <link href="css/account-profile.css" type="text/css" rel="stylesheet">
        <link href="css/responsive-shell.css" type="text/css" rel="stylesheet">
        <link href="css/responsive-content.css" type="text/css" rel="stylesheet">
        <link href="css/responsive-breakpoints.css" type="text/css" rel="stylesheet">
        <script src="https://kit.fontawesome.com/24de11fb10.js" crossorigin="anonymous"></script>
    </head>

    <body>
        <fmt:message key="navigation.account" var="accountLabel"/>
        <fmt:message key="navigation.cart" var="cartLabel"/>
        <fmt:message key="navigation.search" var="searchLabel"/>
        <c:if test="${not empty sessionScope.msg}">
            <p class="store-message" role="status"><c:out value="${sessionScope.msg}"/></p>
            <c:remove var="msg" scope="session"/>
        </c:if>
        <header>

            <div class="logo-div">
                <a href="./">UFF</a>
            </div>

            <div class="dropdown-menu">
                <div class="dropdown ps">
                    <button class="dropbtn" onclick="location.href = 'products?categoryId=1';">Playstation</button>
                    <div class="dropdown-content">
                        <a href="products?categoryId=5"><fmt:message key="ui.consoles"/></a>
                        <a href="products?categoryId=6"><fmt:message key="ui.games"/></a>
                        <a href="products?categoryId=4"><fmt:message key="ui.accessories"/></a>
                    </div>
                </div>
                <div class="dropdown">
                    <button class="dropbtn" onclick="location.href = 'products?categoryId=2';">Xbox</button>
                    <div id="xbox-menu" class="dropdown-content">
                        <a href="products?categoryId=8"><fmt:message key="ui.consoles"/></a>
                        <a href="products?categoryId=9"><fmt:message key="ui.games"/></a>
                        <a href="products?categoryId=7"><fmt:message key="ui.accessories"/></a>
                    </div>
                </div>
                <div class="dropdown nintendo">
                    <button class="dropbtn" onclick="location.href = 'products?categoryId=3';">Nintendo</button>
                    <div id="nintendo-menu" class="dropdown-content">
                        <a href="products?categoryId=11"><fmt:message key="ui.consoles"/></a>
                        <a href="products?categoryId=12"><fmt:message key="ui.games"/></a>
                        <a href="products?categoryId=10"><fmt:message key="ui.accessories"/></a>
                    </div>
                </div>
            </div>
            <div class="canto-header">

                <div class="consulta-header">
                    <form id="search" method="POST" action="products">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                        <input name="search" type="search" aria-label="${searchLabel}" placeholder="${searchLabel}" required>
                    </form>
                </div>

                <div class="login-header">
                    <a href="account"><img alt="${accountLabel}" src="img/my-account.png" width="20" style="vertical-align: middle"></a>
                </div>

                <div class="cart-header">
                    <a href="cart"><img alt="${cartLabel}" src="img/cart.png" width="25" style="vertical-align: middle"></a>
                </div>
                <form method="post" action="language" class="language-switcher">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
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

        <div class="fundo-branco"></div>
