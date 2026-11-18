package br.uff.store.core.services;

import java.util.Date;
import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICartData;
import br.uff.store.core.interfaces.data.IProductData;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.infrastructure.data.CartData;
import br.uff.store.infrastructure.data.ProductData;
import br.uff.store.infrastructure.shared.Helper;

public class CartService implements ICartService {

    private final ICartData carrinhoData;
    private final IProductData produtoData;
    private final Helper helper;
    private static final String TEXTOAVISOESPECIFICACARRINHO = " no carrinho de Id: ";

    public CartService() {
        this(new CartData(), new ProductData(), new Helper());
    }

    public CartService(ICartData carrinhoData, IProductData produtoData) {
        this(carrinhoData, produtoData, new Helper());
    }

    CartService(ICartData carrinhoData, IProductData produtoData, Helper helper) {
        if (carrinhoData == null || produtoData == null || helper == null) {
            throw new IllegalArgumentException("Cart, product, and helper dependencies are required.");
        }
        this.carrinhoData = carrinhoData;
        this.produtoData = produtoData;
        this.helper = helper;
    }

    @Override
    public CartDTO recuperaCartAtivo(Integer carrinhoId, Integer usuarioId, String ip) throws StoreException {
        if (carrinhoId != null) {
            if (Boolean.TRUE.equals(carrinhoData.carrinhoExiste(carrinhoId)) && Boolean.FALSE.equals(carrinhoData.carrinhoVendido(carrinhoId))) {
                if (usuarioId == null) {
                    if (Boolean.TRUE.equals(carrinhoData.carrinhoSemDono(carrinhoId))) {
                        return carrinhoData.encontraCart(carrinhoId);
                    }
                } else {
                    if (Boolean.TRUE.equals(carrinhoData.carrinhoSemDono(carrinhoId))) {
                        carrinhoData.defineDonoDeUmCartSemUserNoMomento(carrinhoId, usuarioId);
                    }
                    if (Boolean.TRUE.equals(carrinhoData.carrinhoDoUser(carrinhoId, usuarioId))) {
                        return carrinhoData.encontraCart(carrinhoId);
                    }
                }
            }
        }
        if (usuarioId != null) {
            List<CartDTO> carrinhosNaoVendidos = carrinhoData.listaCartsDoUserNaoVendidos(usuarioId);
            if (Boolean.FALSE.equals(carrinhosNaoVendidos.isEmpty())) {
                return carrinhosNaoVendidos.get(0);
            }
        }
        String criadoEm = helper.convertDateToString("yyyy-MM-dd HH:mm:ss", new Date());
        carrinhoData.criaCart(ip, criadoEm, usuarioId);
        return carrinhoData.encontraCart(ip, criadoEm, usuarioId);
    }

    @Override
    public void insereProductCart(Integer carrinhoId, Integer produtoId) throws StoreException {
        if (Boolean.TRUE.equals(carrinhoData.carrinhoExiste(carrinhoId)) && Boolean.FALSE.equals(carrinhoData.carrinhoVendido(carrinhoId))) {
            Integer qtd = carrinhoData.quantidadeProductNoCart(carrinhoId, produtoId);
            int currentQuantity = qtd == null ? 0 : qtd;
            validateStock(produtoId, currentQuantity + 1);
            if (currentQuantity > 0) {
                carrinhoData.atualizaQtdDoProductNoCart(carrinhoId, produtoId, currentQuantity + 1);
            } else {
                carrinhoData.adicionaProductNoCart(carrinhoId, produtoId);
            }
            return;
        }
        throw new StoreException("Não foi possível inserir o produto de Id: " + produtoId + TEXTOAVISOESPECIFICACARRINHO + carrinhoId + ".");

    }

    @Override
    public void removeProductCart(Integer carrinhoId, Integer produtoId) throws StoreException {
        if (Boolean.TRUE.equals(carrinhoData.carrinhoExiste(carrinhoId)) && Boolean.FALSE.equals(carrinhoData.carrinhoVendido(carrinhoId))) {
            Integer qtd = carrinhoData.quantidadeProductNoCart(carrinhoId, produtoId);
            if (qtd > 0) {
                carrinhoData.removeProductDoCart(carrinhoId, produtoId);
            } else {
                throw new StoreException("O produto de id: " + produtoId + " não encontra-se no carrinho de id: " + carrinhoId + ".");
            }
            return;
        }
        throw new StoreException("Não foi possível remover o produto de Id: " + produtoId + TEXTOAVISOESPECIFICACARRINHO + carrinhoId + ".");
    }

    @Override
    public List<CartItemDTO> listaProductsCart(Integer id) throws StoreException {
        if (Boolean.TRUE.equals(carrinhoData.carrinhoExiste(id))) {
            return carrinhoData.listaProductsCart(id);
        }
        throw new StoreException("Não foi possível listar os produtos do carrinho de Id: " + id + ".");
    }

    @Override
    public void alteraQuantidadeProductCart(Integer carrinhoId, Integer produtoId, Integer quantidade) throws StoreException {
        if (Boolean.TRUE.equals(carrinhoData.carrinhoExiste(carrinhoId)) && Boolean.FALSE.equals(carrinhoData.carrinhoVendido(carrinhoId))) {
            if (!Boolean.TRUE.equals(carrinhoData.verificaProductNoCart(carrinhoId, produtoId))) {
                throw new StoreException("The product is not in this cart.");
            }
            validateStock(produtoId, quantidade);
            carrinhoData.atualizaQtdDoProductNoCart(carrinhoId, produtoId, quantidade);
            return;
        }
        throw new StoreException("Não foi possível atualizar a quantidade do produto de Id: " + produtoId + TEXTOAVISOESPECIFICACARRINHO + carrinhoId + ".");
    }

    @Override
    public Boolean carrinhoAtivoValido(Integer id, Integer usuarioId) throws StoreException {
        return Boolean.TRUE.equals(carrinhoData.carrinhoExiste(id)) && Boolean.TRUE.equals(carrinhoData.carrinhoDoUser(id, usuarioId)) && Boolean.FALSE.equals(carrinhoData.carrinhoVendido(id)) && Boolean.TRUE.equals(carrinhoData.quantidadeProductsCart(id) > 0);
    }

    @Override
    public Double recuperaPrecoTotalDeUmCart(Integer id) throws StoreException {
        return carrinhoData.recuperaPrecoTotalDeUmCart(id);
    }

    @Override
    public PaginateDTO<List<CartItemDTO>> listaProductsCart(Integer id, Integer itensPorPagina, Integer paginaAtual) throws StoreException {
        return carrinhoData.listaProductsCart(id, itensPorPagina, paginaAtual);
    }

    @Override
    public Integer quantidadeProductNoCart(Integer id, Integer produtoId) throws StoreException {
        return carrinhoData.quantidadeProductNoCart(id, produtoId);
    }

    private void validateStock(Integer productId, Integer requestedQuantity) throws StoreException {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new StoreException("Cart quantity must be greater than zero.");
        }
        ProductDTO product = produtoData.encontraProductPorId(productId);
        if (product.getQuantidade() == null || requestedQuantity > product.getQuantidade()) {
            throw new StoreException("Requested quantity exceeds available stock.");
        }
    }
}
