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

public class LegacyStoreIntegration1Test {
    @Test
    public void testaReviewDuplicada() throws Exception {
        String exMessage = "";
        IReviewService avaliacaoService = new ReviewService();
        IProductService produtoService = new ProductService();
        IUserService usuarioService = new UserService();

        Integer produtoId = produtoService.listaProductsAdm().get(0).getId();
        Integer usuarioId = usuarioService.listaUsers().get(0).getId();

        try {
            ProductReviewInputDTO avaliacaoProductInsertDTO = new ProductReviewInputDTO(usuarioId, produtoId, 4,
                    "Testando descrição!", "Tô testando pois se gravar 2 vezes deu ruim!");

            avaliacaoService.avaliaProduct(avaliacaoProductInsertDTO);
            avaliacaoService.avaliaProduct(avaliacaoProductInsertDTO);
        } catch (Exception ex) {
            exMessage = ex.getMessage();
        }

        if (avaliacaoService.recuperaAvaliacoesDeUmProduct(produtoId).isEmpty()) {
            throw new Exception("A avaliação para o produto de id: " + produtoId + ", não foi realizada!");
        }

        assertEquals("O produto de id: " + produtoId + " já foi avaliado!", exMessage);
    }

    @Test
    public void testaProductReview() throws Exception {
        IReviewService avaliacaoService = new ReviewService();
        IProductService produtoService = new ProductService();
        IUserService usuarioService = new UserService();

        String exMessage = "";

        List<ProductDTO> produtos = produtoService.listaProductsAdm();
        List<UserDTO> usuarios = usuarioService.listaUsers();

        Boolean produtoAvaliado = false;
        Integer indexP = 0;
        Integer indexU = 0;

        while (produtoAvaliado == false) {
            try {
                Integer produtoId = produtos.get(indexP).getId();
                UserDTO usuario = usuarios.get(indexU);

                ProductReviewInputDTO avaliacaoProductInsertDTO = new ProductReviewInputDTO(usuario.getId(),
                        produtoId, 4, "Testando descrição!", "Tô testando avaliacao!");

                if (produtos.size() - 1 > indexP) {
                    indexP++;
                } else {
                    indexP = 0;
                    indexU++;
                    if (indexU > usuarios.size() - 1) {
                        usuarioService.gravaUser(new UserDTO(null, "INclusao p n ficar sem usuario",
                                "ushu@sauhsua.com" + new Random().nextInt(1000), "123",
                                UserRole.CLIENTE.getId()));
                        indexU = 0;
                    }
                }

                List<ProductReviewListDTO> avaliacoesAntes = avaliacaoService
                        .recuperaAvaliacoesDeUmProduct(produtoId);

                avaliacaoService.avaliaProduct(avaliacaoProductInsertDTO);

                List<ProductReviewListDTO> avaliacoesDepois = avaliacaoService
                        .recuperaAvaliacoesDeUmProduct(produtoId);

                produtoAvaliado = avaliacoesAntes.size() < avaliacoesDepois.size() ;
            } catch (Exception ex) {
                exMessage = ex.getMessage();
            }
        }

        assertEquals(true, produtoAvaliado);
    }

    @Test
    public void testaInclusaoAddress() throws Exception {
        IAddressService enderecoService = new AddressService();
        IUserService usuarioService = new UserService();

        Integer usuarioId = usuarioService.listaUsers().get(0).getId();

        Integer qtdAddresssAntesInsercao = enderecoService.listaAddresssPorUserId(usuarioId).size();
        enderecoService.insereAddress(new AddressDTO("Casa", usuarioId, 24230322,
                "Avenida Almirante Ary Parreiras, 6", "Niterói", "RJ", "Brasil"));

        assertEquals(String.valueOf(qtdAddresssAntesInsercao + 1),
                String.valueOf(enderecoService.listaAddresssPorUserId(usuarioId).size()));
    }

    @Test
    public void testaUpdateAddress() throws Exception {
        IAddressService enderecoService = new AddressService();

        Integer usuarioId = 1;

        List<AddressDTO> enderecosDoUser = enderecoService.listaAddresssPorUserId(usuarioId);

        if (enderecosDoUser.size() == 0) {
            enderecoService.insereAddress(new AddressDTO("Casa", usuarioId, 24230322,
                    "Avenida Almirante Ary Parreiras, 6", "Niterói", "RJ", "Brasil"));
            enderecosDoUser = enderecoService.listaAddresssPorUserId(usuarioId);
        }

        AddressDTO primeiroAddress = enderecosDoUser.get(0);
        primeiroAddress.setNome(primeiroAddress.getNome() + "x");

        enderecoService.atualizaAddressPorId(primeiroAddress.getId(), primeiroAddress);

        assertEquals(primeiroAddress.toJson(),
                enderecoService.encontraAddressPorId(primeiroAddress.getId()).toJson());
    }

    @Test
    public void testaExclusaoAddress() throws Exception {
        IAddressService enderecoService = new AddressService();
        IUserService usuarioService = new UserService();

        Integer usuarioId = usuarioService.listaUsers().get(1).getId();
        List<AddressDTO> enderecosDoUser = enderecoService.listaAddresssPorUserId(usuarioId);

        if (enderecosDoUser.size() <= 1) {
            enderecoService.insereAddress(new AddressDTO("Casa", usuarioId, 24230322,
                    "Avenida Almirante Ary Parreiras, 6", "Niterói", "RJ", "Brasil"));
            enderecosDoUser = enderecoService.listaAddresssPorUserId(usuarioId);
        }

        AddressDTO ultimoAddress = enderecosDoUser.get(enderecosDoUser.size() - 1);

        enderecoService.excluiAddressPorId(ultimoAddress.getId(), ultimoAddress.getUserId());

        assertEquals(String.valueOf(enderecosDoUser.size() - 1),
                String.valueOf(enderecoService.listaAddresssPorUserId(usuarioId).size()));
    }

    @Test
    public void testaInclusaoProduct() throws Exception {
        IProductService produtoService = new ProductService();

        Integer qtdProductsAntesInsercao = produtoService.listaProductsAdm().size();
        produtoService.insereProduct(new ProductDTO(null, "Playstation 6", 2000000.55, "Super caro!",
                "url de uma imagem", ProductCategory.PLAYSTATIONCONSOLES.getId(), 1));

        assertEquals(String.valueOf(qtdProductsAntesInsercao + 1),
                String.valueOf(produtoService.listaProductsAdm().size()));
    }

    @Test
    public void testaUpdateProduct() throws Exception {
        IProductService produtoService = new ProductService();

        List<ProductDTO> produtos = produtoService.listaProductsAdm();
        ProductDTO ultimoProduct = produtos.get(produtos.size() - 1);
        ultimoProduct.setNome(ultimoProduct.getNome() + "x");

        produtoService.atualizaProductPorId(ultimoProduct.getId(), ultimoProduct);

        assertEquals(ultimoProduct.toJson(), produtoService.encontraProductPorId(ultimoProduct.getId()).toJson());
    }

    @Test
    public void testaIncrementoDaQuantidadeDeEstoque() throws Exception {
        IProductService produtoService = new ProductService();

        Integer quantidadeIncremento = 2;

        ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);
        produtoService.insereQuantidadeEmEstoqueDoProductPorId(primeiroProduct.getId(), quantidadeIncremento);

        assertEquals(String.valueOf(primeiroProduct.getQuantidade() + quantidadeIncremento),
                String.valueOf(produtoService.encontraProductPorId(primeiroProduct.getId()).getQuantidade()));
    }

    @Test
    public void testaToogleFavoritaProduct() throws Exception {
        IProductService produtoService = new ProductService();

        IUserService usuarioService = new UserService();

        Integer usuarioId = usuarioService.listaUsers().get(0).getId();
        ProductDTO primeiroProduct = produtoService.listaProductsAdm().get(0);

        Boolean antes = produtoService.produtoFavoritadoPeloUser(primeiroProduct.getId(), usuarioId);

        produtoService.usuarioToogleFavoritaProductPorId(primeiroProduct.getId(), usuarioId);
        Boolean depois = produtoService.produtoFavoritadoPeloUser(primeiroProduct.getId(), usuarioId);

        assertNotEquals(String.valueOf(antes), String.valueOf(depois));
    }

}
