package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.PaginateDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.ISaleService;
import br.uff.store.infrastructure.data.SaleData;

public class SaleService implements ISaleService {

    private ISaleData vendaData;
    private ICartService carrinhoService;
    private IAddressService enderecoService;

    public SaleService() {
        vendaData = new SaleData();
        carrinhoService = new CartService();
        enderecoService = new AddressService();
    }

    public SaleService(ICartService carrinhoSrvc, IAddressService enderecoSrvc) {
        vendaData = new SaleData();
        carrinhoService = carrinhoSrvc;
        enderecoService = enderecoSrvc;
    }

    @Override
    public List<SaleDTO> listaSalesDoUser(Integer usuarioId) throws StoreException {
        return vendaData.listaSalesDoUser(usuarioId);
    }

    @Override
    public void gravaSale(Integer usuarioId, Integer carrinhoId, Integer enderecoId) throws StoreException {
        if (Boolean.FALSE.equals(carrinhoService.carrinhoAtivoValido(carrinhoId, usuarioId))) {
            throw new StoreException("O carrinho de id: " + carrinhoId + " não encontra-se válido no momento, verifique se o mesmo possui itens nele, se já foi vendido ou se pertence a outro usuário.");
        }
        if (Boolean.FALSE.equals(enderecoService.verificaAddressDoUser(enderecoId, usuarioId))) {
            throw new StoreException("O endereço escolhido de id:" + enderecoId + " não pertence ao dono do carrinho (usuário de id: " + usuarioId + "), escolha outro endereço.");
        }
        vendaData.gravaSale(carrinhoId, enderecoId);
    }

    @Override
    public PaginateDTO<List<SaleDTO>> listaSalesDoUser(Integer usuarioId, Integer itensPorPagina, Integer paginaAtual) throws StoreException {
        return vendaData.listaSalesDoUser(usuarioId, itensPorPagina, paginaAtual);
    }
}
