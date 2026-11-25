<%@ include file="/WEB-INF/fragments/messages.jspf" %>
<%@page import="br.uff.store.application.security.Html"%>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="br.uff.store.core.dtos.FeaturedProductDTO"%>
<%
    List<FeaturedProductDTO> jogosVitrine = new ArrayList<FeaturedProductDTO>();
    if (request.getAttribute("jogosVitrine") != null) {
        jogosVitrine = (List<FeaturedProductDTO>) request.getAttribute("jogosVitrine");
    }
%>

<fmt:message key="ui.pageTitle.home" var="pageTitle"/>
<jsp:include page="header.jsp">
    <jsp:param name="title" value="${pageTitle}"/>
</jsp:include>
<%
    if (jogosVitrine.size() > 0) {
%>

<div class="slideshow-container">

    <%
        for (FeaturedProductDTO product : jogosVitrine) {
    %>
    <div class="mySlides fade">
        <div style="width:100%;height:600px;background: url('<%= Html.escape(product.getImage()) %>') no-repeat center center;background-size: auto;background-size: cover;">&nbsp;</div>
        <div class="text" onclick="location.href = 'product?productId=<%= product.getId()%>';"><fmt:message key="ui.buyProduct"/></div>
    </div>
    <%
        }
    %>

    <a class="prev" onclick="plusSlides(-1)">&#10094;</a>
    <a class="next" onclick="plusSlides(1)">&#10095;</a>
</div>
<br>

<div style="text-align:center">
    <%
        for (int i = 0; i < jogosVitrine.size(); i++) {
    %>
    <span class="dot" onclick="currentSlide(<%= i%>)"></span>
    <%
        }
    %>
</div>

<script>
    var slideIndex = 1;
    showSlides(slideIndex);

    function plusSlides(n) {
        showSlides(slideIndex += n);
    }

    function currentSlide(n) {
        showSlides(slideIndex = n);
    }

    function showSlides(n) {
        var i;
        var slides = document.getElementsByClassName("mySlides");
        var dots = document.getElementsByClassName("dot");
        if (n > slides.length) {
            slideIndex = 1
        }
        if (n < 1) {
            slideIndex = slides.length
        }
        for (i = 0; i < slides.length; i++) {
            slides[i].style.display = "none";
        }
        for (i = 0; i < dots.length; i++) {
            dots[i].className = dots[i].className.replace(" active", "");
        }
        slides[slideIndex - 1].style.display = "block";
        dots[slideIndex - 1].className += " active";
    }
</script>
<%
    }
%>

<div class="sec-container">
    <div class="sec-img">
        <img alt="Playstation" src="img/ps4.jpg">
        <div class="img-btn"><a href="products?categoryId=1">Playstation</a></div>
    </div>
    <div class="sec-img">
        <img alt="Xbox" src="img/xone.jpg">
        <div class="img-btn"><a href="products?categoryId=2">Xbox</a></div>
    </div>
    <div class="sec-img">
        <img alt="Wii" src="img/wii.jpg">
        <div class="img-btn"><a href="products?categoryId=3">Wii</a></div>
    </div>
</div>


<jsp:include page="footer.jsp"></jsp:include>
