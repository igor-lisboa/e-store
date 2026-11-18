package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ICartService {

    public CartDTO recuperaCartAtivo(Integer carrinhoId, Integer usuarioId, String ip) throws StoreException;

    public void insereProductCart(Integer carrinhoId, Integer produtoId) throws StoreException;

    public void removeProductCart(Integer carrinhoId, Integer produtoId) throws StoreException;

    public List<CartItemDTO> listaProductsCart(Integer id) throws StoreException;

    public PaginateDTO<List<CartItemDTO>> listaProductsCart(Integer id, Integer itensPorPagina, Integer paginaAtual) throws StoreException;

    public void alteraQuantidadeProductCart(Integer carrinhoId, Integer produtoId, Integer quantidade) throws StoreException;

    public Boolean carrinhoAtivoValido(Integer id, Integer usuarioId) throws StoreException;

    public Double recuperaPrecoTotalDeUmCart(Integer id) throws StoreException;

    public Integer quantidadeProductNoCart(Integer id, Integer produtoId) throws StoreException;
}
