package br.uff.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

import org.junit.Test;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.enums.UserRole;
import br.uff.store.core.enums.ProductCategory;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IReviewService;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.ICategoryService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IRoleService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.core.interfaces.services.ISaleService;
import br.uff.store.core.services.ReviewService;
import br.uff.store.core.services.CartService;
import br.uff.store.core.services.CategoryService;
import br.uff.store.core.services.AddressService;
import br.uff.store.core.services.ProductService;
import br.uff.store.core.services.RoleService;
import br.uff.store.core.services.UserService;
import br.uff.store.core.services.SaleService;

public class LegacyStoreIntegration5Test {
    @Test
    public void TestaPercorreSalesUser() throws Exception {
        IUserService usuarioService = new UserService();
        ISaleService vendaService = new SaleService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);

        Integer paginaAtual = 0;
        Integer ultimaPagina = 1;
        Integer itensPorPagina = 2;

        List<SaleDTO> vendasDoPaginate = new ArrayList<>();

        while (ultimaPagina > paginaAtual) {
            paginaAtual += 1;

            PaginateDTO<List<SaleDTO>> vendas = vendaService.listaSalesDoUser(primeiroUser.getId(),
                    itensPorPagina, paginaAtual);

            vendasDoPaginate.addAll(vendas.getDados());

            paginaAtual = vendas.getPaginaAtual();
            ultimaPagina = vendas.getUltimaPagina();
        }

        assertEquals(new Gson().toJson(vendaService.listaSalesDoUser(primeiroUser.getId())),
                new Gson().toJson(vendasDoPaginate));
    }

    @Test
    public void TestaListaRoles() throws Exception {
        IRoleService roleService = new RoleService();

        assertEquals(String.valueOf(roleService.listaRoles().size() > 0), String.valueOf(true));
    }

    @Test
    public void TestaListaCategorys() throws Exception {
        ICategoryService categoriaService = new CategoryService();

        assertEquals(String.valueOf(categoriaService.listaCategorys().size() > 0), String.valueOf(true));
    }

    @Test
    public void TestaListaProductsBanner() throws Exception {
        IProductService produtoService = new ProductService();
        assertEquals(String.valueOf(produtoService.listaProductsBanner().size() <= 3), String.valueOf(true));
    }

    @Test
    public void TestaMostraProductUser() throws Exception {
        IUserService usuarioService = new UserService();
        IProductService produtoService = new ProductService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);
        FeaturedProductDTO primeiroProduct = produtoService.listaProductsBanner().get(0);

        assertEquals(produtoService.mostraProductVitrineParaUser(primeiroProduct.getId(), primeiroUser.getId())
                .getId(), primeiroProduct.getId());
    }
}
