package br.uff.store.application.controllers;

import java.math.BigDecimal;

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
    private static final String PLAYSTATIONSTR = "playstation";
    private static final String CHECKEDSTR = "checked";
    private static final String CONSOLESSTR = "consoles";
    private static final String NEW_RELEASES_KEY = "newReleases";
    private static final String FAVORITES_KEY = "favorites";
    private static final String ACCESSORIES_KEY = "accessories";
    private static final String GAMES_KEY = "games";
    private static final String XBOXSTR = "xbox";
    private static final String WIISTR = "wii";
    private static final String PRODUCT_PAGE = "productPage";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String MAXPAGSTR = "maxPage";

    public ProductsServlet() {
        helper = new Helper();
        productService = StoreServices.products();
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

            session.setAttribute(PLAYSTATIONSTR, (categories.contains(PLAYSTATIONSTR) ? CHECKEDSTR : ""));
            session.setAttribute(XBOXSTR, (categories.contains(XBOXSTR) ? CHECKEDSTR : ""));
            session.setAttribute(WIISTR, (categories.contains(WIISTR) ? CHECKEDSTR : ""));
            session.setAttribute(CONSOLESSTR, (subcategories.contains(CONSOLESSTR) ? CHECKEDSTR : ""));
            session.setAttribute(GAMES_KEY, (subcategories.contains(GAMES_KEY) ? CHECKEDSTR : ""));
            session.setAttribute(ACCESSORIES_KEY, (subcategories.contains(ACCESSORIES_KEY) ? CHECKEDSTR : ""));
            session.setAttribute(FAVORITES_KEY, (specials.contains(FAVORITES_KEY) ? CHECKEDSTR : ""));
            session.setAttribute(NEW_RELEASES_KEY, (specials.contains(NEW_RELEASES_KEY) ? CHECKEDSTR : ""));

            CatalogCategorySelection.apply(request, session, categories, subcategories);

            if(!categories.isEmpty()) {
                filter.setCategories(categories);
            }
            
            if(!subcategories.isEmpty()) {
                filter.setSubcategories(subcategories);
            }

            String espParameter = "";
            if (request.getParameter("special") != null) {
                espParameter = request.getParameter("special");
            }

            if (espParameter.equals(NEW_RELEASES_KEY)) {
                session.setAttribute(NEW_RELEASES_KEY, CHECKEDSTR);
            } else if (espParameter.equals(FAVORITES_KEY)) {
                session.setAttribute(FAVORITES_KEY, CHECKEDSTR);
            }

            if (session.getAttribute(NEW_RELEASES_KEY) != null) {
                if (session.getAttribute(NEW_RELEASES_KEY).toString().equals(CHECKEDSTR) && !specials.contains(NEW_RELEASES_KEY)) {
                    specials.add(NEW_RELEASES_KEY);
                }
            }

            if (session.getAttribute(FAVORITES_KEY) != null) {
                if (session.getAttribute(FAVORITES_KEY).toString().equals(CHECKEDSTR) && !specials.contains(FAVORITES_KEY)) {
                    specials.add(FAVORITES_KEY);
                }
            }

            if (session.getAttribute(CONSOLESSTR) != null) {
                if (session.getAttribute(CONSOLESSTR).toString().equals(CHECKEDSTR) && !subcategories.contains(CONSOLESSTR)) {
                    subcategories.add(CONSOLESSTR);
                }
            }

            if (session.getAttribute(GAMES_KEY) != null) {
                if (session.getAttribute(GAMES_KEY).toString().equals(CHECKEDSTR) && !subcategories.contains(GAMES_KEY)) {
                    subcategories.add(GAMES_KEY);
                }
            }

            if (session.getAttribute(ACCESSORIES_KEY) != null) {
                if (session.getAttribute(ACCESSORIES_KEY).toString().equals(CHECKEDSTR) && !subcategories.contains(ACCESSORIES_KEY)) {
                    subcategories.add(ACCESSORIES_KEY);
                }
            }

            if (session.getAttribute(PLAYSTATIONSTR) != null) {
                if (session.getAttribute(PLAYSTATIONSTR).toString().equals(CHECKEDSTR) && !categories.contains(PLAYSTATIONSTR)) {
                    categories.add(PLAYSTATIONSTR);
                }
            }

            if (session.getAttribute(XBOXSTR) != null) {
                if (session.getAttribute(XBOXSTR).toString().equals(CHECKEDSTR) && !categories.contains(XBOXSTR)) {
                    categories.add(XBOXSTR);
                }
            }

            if (session.getAttribute(WIISTR) != null) {
                if (session.getAttribute(WIISTR).toString().equals(CHECKEDSTR) && !categories.contains(WIISTR)) {
                    categories.add(WIISTR);
                }
            }

            if (specials.contains(FAVORITES_KEY)) {

                if (session.getAttribute("userId") != null) {
                    filter.setUserId(helper.tryParseInteger(session.getAttribute("userId").toString()));
                    filter.setFavoritesOnly(true);
                } else {
                    session.setAttribute("msg", "Sign in to view your favorites.");
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
                    productPage = productPage - 1;
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
            
            session.setAttribute(MAXPAGSTR, pagedProducts.getLastPage());
            session.setAttribute(PRODUCT_PAGE, pagedProducts.getPageNumber());

            request.setAttribute("products", pagedProducts);
            request.getRequestDispatcher("pages/products.jsp").forward(request, response);
        } catch (Exception ex) {
            session.setAttribute("msg", ex.getMessage());
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
        } catch (Exception ex) {
            Logger.getLogger(ProductsServlet.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (Exception ex) {
            Logger.getLogger(ProductsServlet.class.getName()).log(Level.SEVERE, null, ex);
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
