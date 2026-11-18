window.addEventListener("load", function () {
    SwaggerUIBundle({
        url: "openapi.json",
        dom_id: "#swagger-ui",
        deepLinking: true,
        withCredentials: true,
        validatorUrl: null
    });
});
