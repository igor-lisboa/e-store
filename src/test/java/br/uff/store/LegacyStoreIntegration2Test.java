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

public class LegacyStoreIntegration2Test {
    @Test
    public void checaUserAdm() throws Exception {
        IUserService usuarioService = new UserService();
        String errMsg = "";

        usuarioService.listaUsers().forEach(usuario -> {
            try {
                Boolean bdCheckEhAdm = usuarioService.ehAdmin(usuario.getId());
                Boolean enumCheckEhAdm = usuario.getPermissaoId().equals(UserRole.ADM.getId());
                if (Boolean.FALSE.equals(bdCheckEhAdm.equals(enumCheckEhAdm))) {
                    throw new Exception("Validação bd e enum pra verificar se eh adm nao casa...");
                }
            } catch (Exception e) {
                errMsg.concat(e.getMessage());
            }
        });

        assertEquals("", errMsg);
    }

    @Test
    public void testaExclusaoProduct() throws Exception {
        IProductService produtoService = new ProductService();
        List<ProductDTO> produtos = produtoService.listaProductsAdm();

        ProductDTO ultimoProduct = produtos.get(produtos.size() - 1);

        produtoService.excluiProductPorId(ultimoProduct.getId());

        assertEquals(String.valueOf(produtos.size() - 1), String.valueOf(produtoService.listaProductsAdm().size()));
    }

    @Test
    public void testaLoginErro() throws Exception {
        IUserService usuarioService = new UserService();
        String msgErr = "";
        try {
            usuarioService.login("email@errado.com", "xxx");
        } catch (Exception e) {
            msgErr = e.getMessage();
        }
        assertEquals("Invalid email or password.", msgErr);
    }

    @Test
    public void testaLoginSucesso() throws Exception {
        IUserService usuarioService = new UserService();
        String email = "login-test-" + System.nanoTime() + "@example.test";
        UserDTO usuario = usuarioService.gravaUser(new UserDTO(
                null, "Login Test", email, "a-unique-password", UserRole.CLIENTE.getId()));
        UserDTO usuarioLogin = usuarioService.login(email, "a-unique-password");
        assertEquals(usuario.toJson(), usuarioLogin.toJson());
    }

    @Test
    public void testaUpdateUser() throws Exception {
        IUserService usuarioService = new UserService();

        List<UserDTO> usuarios = usuarioService.listaUsers();
        UserDTO primeiroUser = usuarios.get(0);
        primeiroUser.setNome(primeiroUser.getNome() + "x");

        UserDTO usuarioAtualizado = usuarioService.gravaUser(primeiroUser);

        assertEquals(primeiroUser.toJson(), usuarioService.encontraUserPorId(usuarioAtualizado.getId()).toJson());
    }

    @Test
    public void testaInsercaoUser() throws Exception {
        IUserService usuarioService = new UserService();

        UserDTO novoUser = new UserDTO(null, "Teste Insert User", "testando@example.com" + Math.random(),
                "123@SeiQueEhUmaSenhaRuim", UserRole.CLIENTE.getId());

        UserDTO usuarioGravado = usuarioService.gravaUser(novoUser);

        // setando id pra bater igualzinho apos insert
        novoUser.setId(usuarioGravado.getId());

        assertEquals(novoUser.toJson(), usuarioGravado.toJson());
    }

    @Test
    public void testaExcessaoEmailDuplicadoUser() throws Exception {
        IUserService usuarioService = new UserService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);
        primeiroUser.setId(null);

        String msgErr = "";

        try {
            usuarioService.gravaUser(primeiroUser);
        } catch (Exception exc) {
            msgErr = exc.getMessage();
        }

        assertEquals("The email address is already in use.", msgErr);
    }

    @Test
    public void testaListaProductsVitrine() throws Exception {
        IProductService produtoService = new ProductService();

        Integer paginaAtual = 0;
        Integer ultimaPagina = 1;

        Double precoMinimo = 20.1;
        Double precoMaximo = 28.8;

        List<ProductDTO> produtosAdmLista = produtoService.listaProductsAdm();

        produtosAdmLista.removeIf(filter -> filter.getPreco() < precoMinimo || filter.getPreco() > precoMaximo);

        ProductFilterDTO filtro = new ProductFilterDTO();
        filtro.setItensPorPagina(5);
        filtro.setPrecoMinimo(precoMinimo);
        filtro.setPrecoMaximo(precoMaximo);

        List<ProductListDTO> produtosDoPaginate = new ArrayList<>();

        while (ultimaPagina > paginaAtual) {
            paginaAtual += 1;
            filtro.setPaginaAtual(paginaAtual);

            PaginateDTO<List<ProductListDTO>> produtosFiltrado = produtoService.listaProductsVitrine(filtro);

            produtosDoPaginate.addAll(produtosFiltrado.getDados());

            paginaAtual = produtosFiltrado.getPaginaAtual();
            ultimaPagina = produtosFiltrado.getUltimaPagina();
        }

        ArrayList<Integer> produtosAdmListaIds = new ArrayList<>();
        produtosAdmLista.forEach(produto -> produtosAdmListaIds.add(produto.getId()));

        ArrayList<Integer> produtosPaginateIds = new ArrayList<>();
        produtosDoPaginate.forEach(produto -> produtosPaginateIds.add(produto.getId()));

        assertEquals(produtosAdmListaIds.toString(), produtosPaginateIds.toString());
    }

    @Test
    public void TestaInserirProductCart() throws Exception {
        ICartService carrinhoService = new CartService();
        IUserService usuarioService = new UserService();
        IProductService produtoService = new ProductService();

        UserDTO primeiroUser = usuarioService.listaUsers().get(0);
        ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);

        CartDTO carrinho = carrinhoService.recuperaCartAtivo(null, primeiroUser.getId(), "0.0.0.0");

        Integer qtdProductNoCart = carrinhoService.quantidadeProductNoCart(carrinho.getId(),
                primeiroProduct.getId());

        carrinhoService.insereProductCart(carrinho.getId(), primeiroProduct.getId());

        Integer qtdNoCart = carrinhoService.quantidadeProductNoCart(carrinho.getId(), primeiroProduct.getId());

        if (qtdNoCart >= primeiroProduct.getQuantidade()) {
            produtoService.insereQuantidadeEmEstoqueDoProductPorId(primeiroProduct.getId(), qtdNoCart + 10);
        }

        assertEquals(String.valueOf(qtdProductNoCart + 1), String.valueOf(qtdNoCart));
    }

}
