package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ISaleData {

    public List<SaleDTO> listaSalesDoUser(Integer usuarioId) throws StoreException;

    public PaginateDTO<List<SaleDTO>> listaSalesDoUser(Integer usuarioId, Integer itensPorPagina, Integer paginaAtual) throws StoreException;

    public void gravaSale(Integer carrinhoId, Integer enderecoId) throws StoreException;
}
