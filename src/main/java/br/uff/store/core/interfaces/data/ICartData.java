package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ICartData {

    public CartDTO encontraCart(Integer id) throws StoreException;

    public List<CartDTO> listaCartsDoUserNaoVendidos(Integer usuarioId) throws StoreException;

    public PaginateDTO<List<CartItemDTO>> listaProductsCart(Integer id, Integer itensPorPagina, Integer paginaAtual) throws StoreException;

    public Boolean carrinhoVendido(Integer id) throws StoreException;

    public void defineDonoDeUmCartSemUserNoMomento(Integer id, Integer usuarioId) throws StoreException;

    public Boolean carrinhoSemDono(Integer id) throws StoreException;

    public Boolean carrinhoDoUser(Integer id, Integer usuarioId) throws StoreException;

    public Boolean carrinhoExiste(Integer id) throws StoreException;

    public Integer quantidadeProductsCart(Integer id) throws StoreException;

    public Integer quantidadeProductNoCart(Integer id, Integer produtoId) throws StoreException;

    public void criaCart(String ip, String criadoEm, Integer usuarioId) throws StoreException;

    public CartDTO encontraCart(String ip, String criadoEm, Integer usuarioId) throws StoreException;

    public List<CartItemDTO> listaProductsCart(Integer id) throws StoreException;

    public Boolean verificaProductNoCart(Integer id, Integer produtoId) throws StoreException;

    public void adicionaProductNoCart(Integer id, Integer produtoId) throws StoreException;

    public void removeProductDoCart(Integer id, Integer produtoId) throws StoreException;

    public void atualizaQtdDoProductNoCart(Integer id, Integer produtoId, Integer novaQtd) throws StoreException;

    public Double recuperaPrecoTotalDeUmCart(Integer id) throws StoreException;
}
