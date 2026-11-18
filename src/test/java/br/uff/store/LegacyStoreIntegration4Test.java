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

public class LegacyStoreIntegration4Test {
    @Test
    public void TestaCheckoutAddressOutroUser() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();
        ISaleService vendaService = new SaleService();
        IAddressService enderecoService = new AddressService();
        IProductService produtoService = new ProductService();

        List<UserDTO> listaUsers = usuarioService.listaUsers();

        UserDTO primeiroUser = listaUsers.get(0);
        UserDTO segundoUser = listaUsers.get(1);

        List<AddressDTO> enderecosDoUser = enderecoService.listaAddresssPorUserId(segundoUser.getId());
        if (enderecosDoUser.size() == 0) {
            enderecoService.insereAddress(new AddressDTO("Casa", segundoUser.getId(), 24241000,
                    "Rua Doutor Mário Viana, 501", "Niterói", "RJ", "Brasil"));
            enderecosDoUser = enderecoService.listaAddresssPorUserId(segundoUser.getId());
        }
        AddressDTO primeiroAddress = enderecosDoUser.get(0);

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, primeiroUser.getId(), "0.0.0.0");
        List<CartItemDTO> produtos = carrinhoService.listaProductsCart(carrinho.getId());
        if (produtos.size() == 0) {
            ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);

            Integer qtdNoCart = carrinhoService.quantidadeProductNoCart(carrinho.getId(),
                    primeiroProduct.getId());

            if (qtdNoCart >= primeiroProduct.getQuantidade()) {
                produtoService.insereQuantidadeEmEstoqueDoProductPorId(primeiroProduct.getId(), qtdNoCart + 10);
            }

            carrinhoService.insereProductCart(carrinho.getId(), primeiroProduct.getId());
        }

        String msgErro = "";
        String erroEsperado = "O endereço escolhido de id:" + primeiroAddress.getId()
                + " não pertence ao dono do carrinho (usuário de id: " + carrinho.getUserId()
                + "), escolha outro endereço.";

        try {
            vendaService.gravaSale(primeiroUser.getId(), carrinho.getId(), primeiroAddress.getId());
        } catch (Exception e) {
            msgErro = e.getMessage();
        }

        assertEquals(erroEsperado, msgErro);
    }

    @Test
    public void TestaRecuperaCartVendido() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();
        IAddressService enderecoService = new AddressService();
        ISaleService vendaService = new SaleService(carrinhoService, enderecoService);
        IProductService produtoService = new ProductService();

        List<UserDTO> listaUsers = usuarioService.listaUsers();

        UserDTO primeiroUser = listaUsers.get(0);

        List<SaleDTO> vendas = vendaService.listaSalesDoUser(primeiroUser.getId());

        if (vendas.size() == 0) {
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

            vendaService.gravaSale(primeiroUser.getId(), carrinho.getId(), primeiroAddress.getId());

            vendas = vendaService.listaSalesDoUser(primeiroUser.getId());
        }

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(vendas.get(0).getCartId(),
                primeiroUser.getId(), "0.0.0.0");

        assertNotEquals(vendas.get(0).getCartId(), carrinho.getId());
    }

    @Test
    public void TestaRecuperaCartSemInformarId() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, primeiroUser.getId(), "0.0.0.0");

        assertEquals(String.valueOf(carrinho.getId() != null), String.valueOf(true));
    }

    @Test
    public void TestaRecuperaCartSemDono() throws Exception {
        ICartService carrinhoService = new CartService();

        CartDTO carrinhoSemDono = carrinhoService.recuperaCartAtivo(null, null, "0.0.0.0");
        CartDTO carrinho = carrinhoService.recuperaCartAtivo(carrinhoSemDono.getId(), null, "0.0.0.0");

        assertEquals(String.valueOf(carrinhoSemDono.getId()), String.valueOf(carrinho.getId()));
    }

    @Test
    public void TestaRecuperaCartSemUserDepoisDefinir() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);

        CartDTO carrinhoSemDono = carrinhoService.recuperaCartAtivo(null, null, "0.0.0.0");

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(carrinhoSemDono.getId(), primeiroUser.getId(),
                "0.0.0.0");

        assertEquals(String.valueOf(carrinho.getUserId()), String.valueOf(primeiroUser.getId()));
    }

    @Test
    public void TestaPercorreProductsCartPaginate() throws Exception {
        IUserService usuarioService = new UserService();
        ICartService carrinhoService = new CartService();
        IProductService produtoService = new ProductService();

        Double totalPrecoCart = 0.0;

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, primeiroUser.getId(), "0.0.0.0");

        List<ProductDTO> produtos = produtoService.listaProductsAdm();

        produtos.forEach(produto -> {
            try {
                carrinhoService.insereProductCart(carrinho.getId(), produto.getId());

                Integer qtdNoCart = carrinhoService.quantidadeProductNoCart(carrinho.getId(), produto.getId());

                if (qtdNoCart >= produto.getQuantidade()) {
                    produtoService.insereQuantidadeEmEstoqueDoProductPorId(produto.getId(), qtdNoCart + 10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Integer paginaAtual = 0;
        Integer ultimaPagina = 1;
        Integer itensPorPagina = 1;

        List<CartItemDTO> produtosDoCart = new ArrayList<>();

        while (ultimaPagina > paginaAtual) {
            paginaAtual += 1;

            PaginateDTO<List<CartItemDTO>> produtosDoPaginate = carrinhoService
                    .listaProductsCart(carrinho.getId(), itensPorPagina, paginaAtual);

            produtosDoCart.addAll(produtosDoPaginate.getDados());

            paginaAtual = produtosDoPaginate.getPaginaAtual();
            ultimaPagina = produtosDoPaginate.getUltimaPagina();
        }

        ArrayList<Integer> produtosAdmListaIds = new ArrayList<>();
        carrinhoService.listaProductsCart(carrinho.getId())
                .forEach(produto -> produtosAdmListaIds.add(produto.getProductId()));

        ArrayList<Integer> produtosPaginateIds = new ArrayList<>();
        ArrayList<Double> precoXquantidade = new ArrayList<>();
        produtosDoCart.forEach(produto -> {
            produtosPaginateIds.add(produto.getProductId());
            precoXquantidade.add(produto.getPreco() * produto.getQuantidade());
        });

        for (Double precoXqtd : precoXquantidade) {
            totalPrecoCart += precoXqtd;
        }

        assertEquals(true, produtosAdmListaIds.toString().equals(produtosPaginateIds.toString())
                && totalPrecoCart.equals(carrinhoService.recuperaPrecoTotalDeUmCart(carrinho.getId())));
    }

}
