<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<fmt:message key="ui.pageTitle.passwordReset" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>
<fmt:message key="ui.js.passwordsMismatch" var="js_passwordsMismatch"/>

<div class="login-background"></div>
<main class="login-container">
    <c:choose>
        <c:when test="${empty resetToken}">
            <div class="login-column">
                <h2><fmt:message key="ui.resetRequestHeading"/></h2>
                <div class="sub-container">
                    <form method="POST" action="password-reset">
                        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                        <input type="hidden" name="action" value="request">
                        <div class="group">
                            <input id="field-reset-email" name="email" type="email" required maxlength="255">
                            <span class="highlight"></span>
                            <span class="bar"></span>
                            <label for="field-reset-email">E-mail</label>
                        </div>
                        <div class="login-btn">
                            <button type="submit"><fmt:message key="ui.sendResetLink"/></button>
                        </div>
                    </form>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="login-column">
                <h2><fmt:message key="ui.resetCompleteHeading"/></h2>
                <div class="sub-container">
                    <form method="POST" action="password-reset" onsubmit="return validateResetPasswords();">
                        <%@ include file="/WEB-INF/fragments/csrf.jspf" %>
                        <input type="hidden" name="action" value="complete">
                        <input type="hidden" name="token" value="<c:out value="${resetToken}"/>">
                        <div class="group">
                            <input id="resetPassword" name="password" type="password" required minlength="8" maxlength="255" autocomplete="new-password">
                            <span class="highlight"></span>
                            <span class="bar"></span>
                            <label for="resetPassword"><fmt:message key="ui.newPassword"/></label>
                        </div>
                        <div class="group">
                            <input id="resetPasswordConfirmation" type="password" required autocomplete="new-password">
                            <span class="highlight"></span>
                            <span class="bar"></span>
                            <label for="resetPasswordConfirmation"><fmt:message key="ui.confirmPassword"/></label>
                        </div>
                        <script>
                            function validateResetPasswords() {
                                if (document.getElementById("resetPassword").value != document.getElementById("resetPasswordConfirmation").value) {
                                    alert("${js_passwordsMismatch}");
                                    return false;
                                }
                                return true;
                            }
                        </script>
                        <div class="login-btn">
                            <button type="submit"><fmt:message key="ui.setNewPassword"/></button>
                        </div>
                    </form>
                </div>
            </div>
        </c:otherwise>
    </c:choose>
</main>

<jsp:include page="footer.jsp"></jsp:include>
