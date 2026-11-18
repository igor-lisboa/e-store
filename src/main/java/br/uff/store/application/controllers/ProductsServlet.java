package br.uff.store.application.controllers;

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
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.services.ProductService;
import br.uff.store.infrastructure.shared.Helper;

@WebServlet("/products")
public class ProductsServlet extends HttpServlet {
    private final IProductService produtoService;
    private final Helper helper;
    private static final String PESQUISASTR = "search";
    private static final String PLAYSTATIONSTR = "playstation";
    private static final String CHECKEDSTR = "checked";
    private static final String CONSOLESSTR = "consoles";
    private static final String LANCAMENTOSSTR = "newReleases";
    private static final String FAVORITOSSTR = "favorites";
    private static final String ACESSORIOSSTR = "accessories";
    private static final String JOGOSSTR = "games";
    private static final String XBOXSTR = "xbox";
    private static final String WIISTR = "wii";
    private static final String PRODUTOSPAGSTR = "ProductsPag";
    private static final String MAXPAGSTR = "maxPag";

    public ProductsServlet() {
        helper = new Helper();
        produtoService = new ProductService();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // pega sessao
        HttpSession session = request.getSession();

        try {
            ProductFilterDTO filtro = new ProductFilterDTO();
            filtro.setItensPorPagina(5);
            filtro.setPaginaAtual(1);

            if (request.getParameter(PESQUISASTR) != null) {
                session.setAttribute(PESQUISASTR, request.getParameter(PESQUISASTR));
            }

            if (session.getAttribute(PESQUISASTR) != null) {
                filtro.setPesquisa(session.getAttribute(PESQUISASTR).toString());
            }

            // pega esp
            String[] especiais = request.getParameterValues("special");

            // pega categorias
            String[] categorias = request.getParameterValues("category");

            // pega sub categorias
            String[] subCategorys = request.getParameterValues("subCategory");

            // Converte String Array de categorias p Lista
            List<String> listaCategorys = new ArrayList<>();
            if (categorias != null) {
                listaCategorys = new ArrayList<>(Arrays.asList(categorias));
            }

            // Converte String Array de sub categorias p Lista
            List<String> listaSubCategorys = new ArrayList<>();
            if (subCategorys != null) {
                listaSubCategorys = new ArrayList<>(Arrays.asList(subCategorys));
            }

            // Converte String Array de especiais p Lista
            List<String> listaEsp = new ArrayList<>();
            if (especiais != null) {
                listaEsp = new ArrayList<>(Arrays.asList(especiais));
            }

            // carrega categorias com base nos selects checados
            session.setAttribute(PLAYSTATIONSTR, (listaCategorys.contains(PLAYSTATIONSTR) ? CHECKEDSTR : ""));
            session.setAttribute(XBOXSTR, (listaCategorys.contains(XBOXSTR) ? CHECKEDSTR : ""));
            session.setAttribute(WIISTR, (listaCategorys.contains(WIISTR) ? CHECKEDSTR : ""));
            session.setAttribute(CONSOLESSTR, (listaSubCategorys.contains(CONSOLESSTR) ? CHECKEDSTR : ""));
            session.setAttribute(JOGOSSTR, (listaSubCategorys.contains(JOGOSSTR) ? CHECKEDSTR : ""));
            session.setAttribute(ACESSORIOSSTR, (listaSubCategorys.contains(ACESSORIOSSTR) ? CHECKEDSTR : ""));
            session.setAttribute(FAVORITOSSTR, (listaEsp.contains(FAVORITOSSTR) ? CHECKEDSTR : ""));
            session.setAttribute(LANCAMENTOSSTR, (listaEsp.contains(LANCAMENTOSSTR) ? CHECKEDSTR : ""));

            CatalogCategorySelection.apply(request, session, listaCategorys, listaSubCategorys);

            if(!listaCategorys.isEmpty()) {
                filtro.setCategorys(listaCategorys);
            }
            
            if(!listaSubCategorys.isEmpty()) {
                filtro.setSubCategorys(listaSubCategorys);
            }

            // verifica se chamou um filtro especial
            String espParameter = "";
            if (request.getParameter("special") != null) {
                espParameter = request.getParameter("special");
            }

            if (espParameter.equals(LANCAMENTOSSTR)) {
                session.setAttribute(LANCAMENTOSSTR, CHECKEDSTR);
            } else if (espParameter.equals(FAVORITOSSTR)) {
                session.setAttribute(FAVORITOSSTR, CHECKEDSTR);
            }

            // pega esp lancamentos q n esta na lista de esp e insere
            if (session.getAttribute(LANCAMENTOSSTR) != null) {
                if (session.getAttribute(LANCAMENTOSSTR).toString().equals(CHECKEDSTR) && !listaEsp.contains(LANCAMENTOSSTR)) {
                    listaEsp.add(LANCAMENTOSSTR);
                }
            }

            // pega esp favoritos q n esta na lista de esp e insere
            if (session.getAttribute(FAVORITOSSTR) != null) {
                if (session.getAttribute(FAVORITOSSTR).toString().equals(CHECKEDSTR) && !listaEsp.contains(FAVORITOSSTR)) {
                    listaEsp.add(FAVORITOSSTR);
                }
            }

            // pega sub categoria console q n esta na lista de sub categorias e insere
            if (session.getAttribute(CONSOLESSTR) != null) {
                if (session.getAttribute(CONSOLESSTR).toString().equals(CHECKEDSTR) && !listaSubCategorys.contains(CONSOLESSTR)) {
                    listaSubCategorys.add(CONSOLESSTR);
                }
            }
            // pega  sub categoria jogo q n esta na lista de sub categorias e insere
            if (session.getAttribute(JOGOSSTR) != null) {
                if (session.getAttribute(JOGOSSTR).toString().equals(CHECKEDSTR) && !listaSubCategorys.contains(JOGOSSTR)) {
                    listaSubCategorys.add(JOGOSSTR);
                }
            }
            // pega sub categoria acessorio q n esta na lista de sub categorias e insere
            if (session.getAttribute(ACESSORIOSSTR) != null) {
                if (session.getAttribute(ACESSORIOSSTR).toString().equals(CHECKEDSTR) && !listaSubCategorys.contains(ACESSORIOSSTR)) {
                    listaSubCategorys.add(ACESSORIOSSTR);
                }
            }
            // pega categoria playstation q n esta na lista de categorias e insere
            if (session.getAttribute(PLAYSTATIONSTR) != null) {
                if (session.getAttribute(PLAYSTATIONSTR).toString().equals(CHECKEDSTR) && !listaCategorys.contains(PLAYSTATIONSTR)) {
                    listaCategorys.add(PLAYSTATIONSTR);
                }
            }
            // pega categoria xbox q n esta na lista de categorias e insere
            if (session.getAttribute(XBOXSTR) != null) {
                if (session.getAttribute(XBOXSTR).toString().equals(CHECKEDSTR) && !listaCategorys.contains(XBOXSTR)) {
                    listaCategorys.add(XBOXSTR);
                }
            }
            // pega categoria wii q n esta na lista de categorias e insere
            if (session.getAttribute(WIISTR) != null) {
                if (session.getAttribute(WIISTR).toString().equals(CHECKEDSTR) && !listaCategorys.contains(WIISTR)) {
                    listaCategorys.add(WIISTR);
                }
            }

            // filtra favoritos
            if (listaEsp.contains(FAVORITOSSTR)) {
                // se tem usuario logado mostra filtra produtos por favoritos
                if (session.getAttribute("userId") != null) {
                    filtro.setUserId(helper.tryParseInteger(session.getAttribute("userId").toString()));
                    filtro.setApenasFavoritados(true);
                } else {
                    session.setAttribute("msg", "Realize login para ver seus favoritos!");
                    response.sendRedirect("account?redirect=products");
                    return;
                }
            }

            if (listaEsp.contains(LANCAMENTOSSTR)) {
                filtro.setApenasLancamentos(true);
            }

            // filtra menor preco
            if (request.getParameter("price_min") != null) {
                Double priceMin = helper.tryParseDouble(request.getParameter("price_min"));
                if (priceMin != null) {
                    session.setAttribute("priceMin", priceMin);
                    filtro.setPrecoMinimo(priceMin);
                }
            }

            // filtra maior preco
            if (request.getParameter("price_max") != null) {
                Double priceMax = helper.tryParseDouble(request.getParameter("price_max"));
                if (priceMax != null) {
                    session.setAttribute("priceMax", priceMax);
                    filtro.setPrecoMaximo(priceMax);
                }
            }

            // define pag atual
            Integer produtosPag = 1;
            if (session.getAttribute(PRODUTOSPAGSTR) != null) {
                produtosPag = Integer.valueOf(session.getAttribute(PRODUTOSPAGSTR).toString());
            }

            // recupera acao solicitada se existir
            String action = "";
            if(request.getParameter("action")!=null) {
                action = request.getParameter("action");
            }

            switch (action) {
                case "previous": {
                    produtosPag = produtosPag - 1;
                    break;
                }
                case "next": {
                    produtosPag = produtosPag + 1;
                    break;
                }
                default: {
                    break;
                }
            }

            filtro.setPaginaAtual(produtosPag);

            PaginateDTO<List<ProductListDTO>> produtosPaginados = produtoService.listaProductsVitrine(filtro);
            
            session.setAttribute(MAXPAGSTR, produtosPaginados.getUltimaPagina());
            session.setAttribute(PRODUTOSPAGSTR, produtosPaginados.getPaginaAtual());

            request.setAttribute("products", produtosPaginados);
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
        return "Servlet que lista produtos.";
    }// </editor-fold>

}
