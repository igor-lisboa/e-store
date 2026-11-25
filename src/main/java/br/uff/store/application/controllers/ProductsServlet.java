package br.uff.store.application.controllers;

import java.math.BigDecimal;

import br.uff.store.application.FlashMessages;
import br.uff.store.application.StoreServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.services.ProductService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/products")
public class ProductsServlet extends HttpServlet {
    private final IProductService productService;
    private final Helper helper;
    private static final String SEARCH_KEY = "search";
    private static final String PLAYSTATION_KEY = "playstation";
    private static final String CHECKED = "checked";
    private static final String CONSOLES_KEY = "consoles";
    private static final String NEW_RELEASES_KEY = "newReleases";
    private static final String FAVORITES_KEY = "favorites";
    private static final String ACCESSORIES_KEY = "accessories";
    private static final String GAMES_KEY = "games";
    private static final String XBOX_KEY = "xbox";
    private static final String WII_KEY = "wii";
    private static final String PRODUCT_PAGE = "productPage";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String MAX_PAGE_KEY = "maxPage";

    public ProductsServlet() {
        this(StoreServices.products(), new Helper());
    }

    ProductsServlet(IProductService productService, Helper helper) {
        this.productService = productService;
        this.helper = helper;
    }

    /** Uses the optional pageSize query value when it is within 1-100; otherwise the storefront default. */
    static int resolvePageSize(String requested) {
        Integer size = new Helper().tryParseInteger(requested);
        return size != null && size >= 1 && size <= 100 ? size : DEFAULT_PAGE_SIZE;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();

        try {
            ProductFilterDTO filter = new ProductFilterDTO();
            filter.setPageSize(resolvePageSize(request.getParameter("pageSize")));
            filter.setPageNumber(1);

            if (request.getParameter(SEARCH_KEY) != null) {
                session.setAttribute(SEARCH_KEY, request.getParameter(SEARCH_KEY));
            }

            if (session.getAttribute(SEARCH_KEY) != null) {
                filter.setSearch(session.getAttribute(SEARCH_KEY).toString());
            }

            String[] specialValues = request.getParameterValues("special");

            String[] requestedCategories = request.getParameterValues("category");

            String[] requestedSubcategories = request.getParameterValues("subCategory");

            List<String> categories = new ArrayList<>();
            if (requestedCategories != null) {
                categories = new ArrayList<>(Arrays.asList(requestedCategories));
            }

            List<String> subcategories = new ArrayList<>();
            if (requestedSubcategories != null) {
                subcategories = new ArrayList<>(Arrays.asList(requestedSubcategories));
            }

            List<String> specials = new ArrayList<>();
            if (specialValues != null) {
                specials = new ArrayList<>(Arrays.asList(specialValues));
            }

            session.setAttribute(PLAYSTATION_KEY, (categories.contains(PLAYSTATION_KEY) ? CHECKED : ""));
            session.setAttribute(XBOX_KEY, (categories.contains(XBOX_KEY) ? CHECKED : ""));
            session.setAttribute(WII_KEY, (categories.contains(WII_KEY) ? CHECKED : ""));
            session.setAttribute(CONSOLES_KEY, (subcategories.contains(CONSOLES_KEY) ? CHECKED : ""));
            session.setAttribute(GAMES_KEY, (subcategories.contains(GAMES_KEY) ? CHECKED : ""));
            session.setAttribute(ACCESSORIES_KEY, (subcategories.contains(ACCESSORIES_KEY) ? CHECKED : ""));
            session.setAttribute(FAVORITES_KEY, (specials.contains(FAVORITES_KEY) ? CHECKED : ""));
            session.setAttribute(NEW_RELEASES_KEY, (specials.contains(NEW_RELEASES_KEY) ? CHECKED : ""));

            CatalogCategorySelection.apply(request, session, categories, subcategories);

            if(!categories.isEmpty()) {
                filter.setCategories(categories);
            }
            
            if(!subcategories.isEmpty()) {
                filter.setSubcategories(subcategories);
            }

            String specialParameter = "";
            if (request.getParameter("special") != null) {
                specialParameter = request.getParameter("special");
            }

            if (specialParameter.equals(NEW_RELEASES_KEY)) {
                session.setAttribute(NEW_RELEASES_KEY, CHECKED);
            } else if (specialParameter.equals(FAVORITES_KEY)) {
                session.setAttribute(FAVORITES_KEY, CHECKED);
            }

            if (session.getAttribute(NEW_RELEASES_KEY) != null) {
                if (session.getAttribute(NEW_RELEASES_KEY).toString().equals(CHECKED) && !specials.contains(NEW_RELEASES_KEY)) {
                    specials.add(NEW_RELEASES_KEY);
                }
            }

            if (session.getAttribute(FAVORITES_KEY) != null) {
                if (session.getAttribute(FAVORITES_KEY).toString().equals(CHECKED) && !specials.contains(FAVORITES_KEY)) {
                    specials.add(FAVORITES_KEY);
                }
            }

            if (session.getAttribute(CONSOLES_KEY) != null) {
                if (session.getAttribute(CONSOLES_KEY).toString().equals(CHECKED) && !subcategories.contains(CONSOLES_KEY)) {
                    subcategories.add(CONSOLES_KEY);
                }
            }

            if (session.getAttribute(GAMES_KEY) != null) {
                if (session.getAttribute(GAMES_KEY).toString().equals(CHECKED) && !subcategories.contains(GAMES_KEY)) {
                    subcategories.add(GAMES_KEY);
                }
            }

            if (session.getAttribute(ACCESSORIES_KEY) != null) {
                if (session.getAttribute(ACCESSORIES_KEY).toString().equals(CHECKED) && !subcategories.contains(ACCESSORIES_KEY)) {
                    subcategories.add(ACCESSORIES_KEY);
                }
            }

            if (session.getAttribute(PLAYSTATION_KEY) != null) {
                if (session.getAttribute(PLAYSTATION_KEY).toString().equals(CHECKED) && !categories.contains(PLAYSTATION_KEY)) {
                    categories.add(PLAYSTATION_KEY);
                }
            }

            if (session.getAttribute(XBOX_KEY) != null) {
                if (session.getAttribute(XBOX_KEY).toString().equals(CHECKED) && !categories.contains(XBOX_KEY)) {
                    categories.add(XBOX_KEY);
                }
            }

            if (session.getAttribute(WII_KEY) != null) {
                if (session.getAttribute(WII_KEY).toString().equals(CHECKED) && !categories.contains(WII_KEY)) {
                    categories.add(WII_KEY);
                }
            }

            if (specials.contains(FAVORITES_KEY)) {

                if (session.getAttribute("userId") != null) {
                    filter.setUserId(helper.tryParseInteger(session.getAttribute("userId").toString()));
                    filter.setFavoritesOnly(true);
                } else {
                    session.setAttribute("msg", FlashMessages.text("Sign in to view your favorites.", FlashMessages.localeOf(session)));
                    response.sendRedirect("account?redirect=products");
                    return;
                }
            }

            if (specials.contains(NEW_RELEASES_KEY)) {
                filter.setNewReleasesOnly(true);
            }

            if (request.getParameter("price_min") != null) {
                BigDecimal priceMin = helper.tryParseDecimal(request.getParameter("price_min"));
                if (priceMin != null) {
                    session.setAttribute("priceMin", priceMin);
                    filter.setMinimumPrice(priceMin);
                }
            }

            if (request.getParameter("price_max") != null) {
                BigDecimal priceMax = helper.tryParseDecimal(request.getParameter("price_max"));
                if (priceMax != null) {
                    session.setAttribute("priceMax", priceMax);
                    filter.setMaximumPrice(priceMax);
                }
            }

            Integer productPage = 1;
            if (session.getAttribute(PRODUCT_PAGE) != null) {
                productPage = Integer.valueOf(session.getAttribute(PRODUCT_PAGE).toString());
            }

            String action = "";
            if(request.getParameter("action")!=null) {
                action = request.getParameter("action");
            }

            switch (action) {
                case "previous": {
                    productPage = Math.max(1, productPage - 1);
                    break;
                }
                case "next": {
                    productPage = productPage + 1;
                    break;
                }
                default: {
                    break;
                }
            }

            filter.setPageNumber(productPage);

            PaginationDTO<List<ProductListDTO>> pagedProducts = productService.listStorefrontProducts(filter);
            
            session.setAttribute(MAX_PAGE_KEY, pagedProducts.getLastPage());
            session.setAttribute(PRODUCT_PAGE, pagedProducts.getPageNumber());

            request.setAttribute("products", pagedProducts);
            request.getRequestDispatcher("pages/products.jsp").forward(request, response);
        } catch (Exception failure) {
            session.setAttribute("msg", FlashMessages.forFailure(failure, FlashMessages.localeOf(session)));
            response.sendRedirect("");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception failure) {
            Logger.getLogger(ProductsServlet.class.getName()).log(Level.SEVERE, null, failure);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception failure) {
            Logger.getLogger(ProductsServlet.class.getName()).log(Level.SEVERE, null, failure);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Handles storefront requests.";
    }// </editor-fold>

}
