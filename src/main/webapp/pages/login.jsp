<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@page import="br.uff.store.application.security.Html"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%

    if (session.getAttribute("userId") != null) {
        response.sendRedirect("account");
    }
%>

<fmt:message key="ui.pageTitle.login" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>
<fmt:message key="ui.js.passwordsMismatch" var="js_passwordsMismatch"/>

<div class="login-background"></div>
<main class="login-container">

    <div class="login-column">
        <h2><fmt:message key="ui.login"/></h2>
        <div class="sub-container">
            <form method="POST" action="account">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

                <input name="redirect" style="display:none;" value="<%= Html.escape(request.getAttribute("redirect")) %>">

                <div class="group">
                    <input id="field-email" name="email" type="email" required value="${fn:escapeXml(cookie['loginEmail'].value)}">
                    <span class="highlight"></span>
                    <span class="bar"></span>
                    <label for="field-email">E-mail</label>
                </div>

                <div class="group">
                    <input id="field-password" name="password" type="password" required maxlength="255" autocomplete="current-password">
                    <span class="highlight"></span>
                    <span class="bar"></span>
                    <label for="field-password"><fmt:message key="ui.password"/></label>
                </div>

                <div class="forgot-pswrd">
                    <input value="1" name="remember" id="remember" type="checkbox">
                    <label for="remember"><fmt:message key="ui.rememberEmail"/></label>
                </div>
                <p class="forgot-link"><a href="password-reset"><fmt:message key="ui.forgotPassword"/></a></p>

                <div class="login-btn">
                    <button type="submit" name="action" value="login"><fmt:message key="ui.signIn"/></button>
                </div>

            </form>
        </div>
    </div>


    <div class="register-column">
        <h2><fmt:message key="ui.createAccount"/></h2>
        <div class="sub-container">
            <form method="POST" action="account">
        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>

                <input name="redirect" style="display:none;" value="<%= Html.escape(request.getAttribute("redirect")) %>">

                <div class="group">
                    <input id="field-name" name="name" type="text" required>
                    <span class="highlight"></span>
                    <span class="bar"></span>
                    <label for="field-name"><fmt:message key="ui.fullName"/></label>
                </div>

                <div class="group">
                    <input id="field-email-2" name="email" type="email" required>
                    <span class="highlight"></span>
                    <span class="bar"></span>
                    <label for="field-email-2">E-mail</label>
                </div>

                <div class="group">
                    <input name="password" id="registrationPassword" type="password" required minlength="8" maxlength="255" autocomplete="new-password">
                    <span class="highlight"></span>
                    <span class="bar"></span>
                    <label for="registrationPassword"><fmt:message key="ui.password"/></label>
                </div>

                <div class="group">
                    <input id="confirmationPassword" type="password" required>
                    <span class="highlight"></span>
                    <span class="bar"></span>
                    <label for="confirmationPassword"><fmt:message key="ui.confirmPassword"/></label>
                </div>
                <script>
                    function validatePasswords() {
                        var password = document.getElementById("registrationPassword");
                        var conf = document.getElementById("confirmationPassword");
                        if (password.value != conf.value) {
                            alert("${js_passwordsMismatch}");
                            return false;
                        } else {
                            return true;
                        }
                    }
                </script>
                <div class="login-btn">
                    <button onclick="return validatePasswords();" name="action" value="register" type="submit"><fmt:message key="ui.createAccount"/></button>
                </div>

            </form>
        </div>
    </div>

</main>


<jsp:include page="footer.jsp"></jsp:include>
