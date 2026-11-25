package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ISaleService {

    public List<SaleDTO> listUserSales(Integer userId) throws StoreException;

    public PaginationDTO<List<SaleDTO>> listUserSales(Integer userId, Integer pageSize,
            Integer pageNumber) throws StoreException;

    public void recordSale(Integer userId, Integer cartId, Integer addressId) throws StoreException;

    public void cancelSale(Integer userId, Integer saleId) throws StoreException;
}
