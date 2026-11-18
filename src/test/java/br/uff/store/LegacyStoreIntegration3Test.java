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

public class LegacyStoreIntegration3Test {
    @Test
    public void TestaRemoverProductCart() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();
        IProductService produtoService = new ProductService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);
        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, primeiroUser.getId(), "0.0.0.0");

        List<CartItemDTO> produtos = carrinhoService.listaProductsCart(carrinho.getId());

        if (produtos.size() == 0) {

            ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);

            carrinhoService.insereProductCart(carrinho.getId(), primeiroProduct.getId());

            Integer qtdNoCart = carrinhoService.quantidadeProductNoCart(carrinho.getId(),
                    primeiroProduct.getId());

            if (qtdNoCart >= primeiroProduct.getQuantidade()) {
                produtoService.insereQuantidadeEmEstoqueDoProductPorId(primeiroProduct.getId(), qtdNoCart + 10);
            }

            produtos = carrinhoService.listaProductsCart(carrinho.getId());
        }

        CartItemDTO primeiroProduct = produtos.get(0);
        carrinhoService.removeProductCart(carrinho.getId(), primeiroProduct.getProductId());

        assertEquals(String.valueOf(produtos.size() - 1),
                String.valueOf(carrinhoService.listaProductsCart(carrinho.getId()).size()));
    }

    @Test
    public void TestaCheckout() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();
        IAddressService enderecoService = new AddressService();
        ISaleService vendaService = new SaleService(carrinhoService, enderecoService);
        IProductService produtoService = new ProductService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);

        List<AddressDTO> enderecosDoUser = enderecoService.listaAddresssPorUserId(primeiroUser.getId());
        if (enderecosDoUser.size() == 0) {
            enderecoService.insereAddress(new AddressDTO("Casa", primeiroUser.getId(), 24230322,
                    "Avenida Almirante Ary Parreiras, 6", "Niterói", "RJ", "Brasil"));
            enderecosDoUser = enderecoService.listaAddresssPorUserId(primeiroUser.getId());
        }
        AddressDTO primeiroAddress = enderecosDoUser.get(0);

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, primeiroUser.getId(), "0.0.0.0");
        List<CartItemDTO> produtos = carrinhoService.listaProductsCart(carrinho.getId());
        if (produtos.size() == 0) {

            ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);

            carrinhoService.insereProductCart(carrinho.getId(), primeiroProduct.getId());
        }

        produtos = carrinhoService.listaProductsCart(carrinho.getId());

        produtos.forEach(produto -> {
            try {
                Integer qtdNoCart = carrinhoService.quantidadeProductNoCart(carrinho.getId(),
                        produto.getProductId());

                if (qtdNoCart >= produto.getQuantidadeEmEstoque()) {
                    produtoService.insereQuantidadeEmEstoqueDoProductPorId(produto.getProductId(), qtdNoCart + 10);
                }
            } catch (StoreException e) {
                e.printStackTrace();
            }
        });

        Integer qtdSalesDoUserAntes = vendaService.listaSalesDoUser(primeiroUser.getId()).size();

        vendaService.gravaSale(primeiroUser.getId(), carrinho.getId(), primeiroAddress.getId());

        assertEquals(String.valueOf(qtdSalesDoUserAntes + 1),
                String.valueOf(vendaService.listaSalesDoUser(primeiroUser.getId()).size()));
    }

    @Test
    public void TestaCheckoutCartDeOutroUser() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();
        IAddressService enderecoService = new AddressService();
        IProductService produtoService = new ProductService();
        ISaleService vendaService = new SaleService(carrinhoService, enderecoService);

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);
        UserDTO segundoUser = usuarioService.listaUsers().get(1);

        String errMsg = "";

        List<AddressDTO> enderecosDoUser = enderecoService.listaAddresssPorUserId(primeiroUser.getId());
        if (enderecosDoUser.size() == 0) {
            enderecoService.insereAddress(new AddressDTO("Casa", primeiroUser.getId(), 24230322,
                    "Avenida Almirante Ary Parreiras, 6", "Niterói", "RJ", "Brasil"));
            enderecosDoUser = enderecoService.listaAddresssPorUserId(primeiroUser.getId());
        }
        AddressDTO primeiroAddress = enderecosDoUser.get(0);

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, segundoUser.getId(), "0.0.0.0");
        List<CartItemDTO> produtos = carrinhoService.listaProductsCart(carrinho.getId());
        if (produtos.size() == 0) {
            ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);
            carrinhoService.insereProductCart(carrinho.getId(), primeiroProduct.getId());
        }

        try {
            vendaService.gravaSale(primeiroUser.getId(), carrinho.getId(), primeiroAddress.getId());
        } catch (Exception ex) {
            errMsg = ex.getMessage();
        }

        assertEquals("O carrinho de id: " + carrinho.getId()
                + " não encontra-se válido no momento, verifique se o mesmo possui itens nele, se já foi vendido ou se pertence a outro usuário.",
                errMsg);
    }

    @Test
    public void TestaCheckoutCartETentaAlterarQtdProduct() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();
        IAddressService enderecoService = new AddressService();
        IProductService produtoService = new ProductService();
        ISaleService vendaService = new SaleService(carrinhoService, enderecoService);

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);

        String errMsg = "";

        List<AddressDTO> enderecosDoUser = enderecoService.listaAddresssPorUserId(primeiroUser.getId());
        if (enderecosDoUser.size() == 0) {
            enderecoService.insereAddress(new AddressDTO("Casa", primeiroUser.getId(), 24230322,
                    "Avenida Almirante Ary Parreiras, 6", "Niterói", "RJ", "Brasil"));
            enderecosDoUser = enderecoService.listaAddresssPorUserId(primeiroUser.getId());
        }
        AddressDTO primeiroAddress = enderecosDoUser.get(0);

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, primeiroUser.getId(), "0.0.0.0");
        List<CartItemDTO> produtos = carrinhoService.listaProductsCart(carrinho.getId());
        if (produtos.size() == 0) {
            ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);

            carrinhoService.insereProductCart(carrinho.getId(), primeiroProduct.getId());

            Integer qtdNoCart = carrinhoService.quantidadeProductNoCart(carrinho.getId(),
                    primeiroProduct.getId());

            if (qtdNoCart >= primeiroProduct.getQuantidade()) {
                produtoService.insereQuantidadeEmEstoqueDoProductPorId(primeiroProduct.getId(), qtdNoCart + 10);
            }

        }

        vendaService.gravaSale(primeiroUser.getId(), carrinho.getId(), primeiroAddress.getId());

        CartItemDTO produtoCart = carrinhoService.listaProductsCart(carrinho.getId()).get(0);

        try {
            carrinhoService.alteraQuantidadeProductCart(carrinho.getId(), produtoCart.getId(),
                    produtoCart.getQuantidade() + 2);
        } catch (Exception ex) {
            errMsg = ex.getMessage();
        }

        assertEquals("Não foi possível atualizar a quantidade do produto de Id: " + produtoCart.getId()
                + " no carrinho de Id: " + carrinho.getId() + ".", errMsg);
    }

}
