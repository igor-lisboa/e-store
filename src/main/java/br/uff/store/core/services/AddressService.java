package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IAddressData;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.infrastructure.data.AddressData;

public class AddressService implements IAddressService {

    private final IAddressData enderecoData;

    public AddressService() {
        this(new AddressData());
    }

    public AddressService(IAddressData enderecoData) {
        if (enderecoData == null) {
            throw new IllegalArgumentException("Address data dependency is required.");
        }
        this.enderecoData = enderecoData;
    }

    @Override
    public AddressDTO encontraAddressPorId(Integer id) throws StoreException {
        return enderecoData.encontraAddressPorId(id);
    }

    @Override
    public void excluiAddressPorId(Integer id, Integer usuarioId) throws StoreException {
        if (!Boolean.TRUE.equals(enderecoData.verificaAddressDoUser(id, usuarioId))) {
            throw new StoreException("The address does not belong to this account.");
        }
        if (Boolean.TRUE.equals(enderecoData.enderecoFoiUsadoEmAlgumaSale(id))) {
            throw new StoreException("Esse endereço não pode ser excluído pois foi usado para realizar uma venda...");
        }
        enderecoData.excluiAddressPorId(id);
    }

    @Override
    public AddressDTO atualizaAddressPorId(Integer id, AddressDTO endereco) throws StoreException {
        if (!Boolean.TRUE.equals(enderecoData.verificaAddressDoUser(id, endereco.getUserId()))) {
            throw new StoreException("The address does not belong to this account.");
        }
        enderecoData.atualizaAddressPorId(id, endereco);
        return this.encontraAddressPorId(id);
    }

    @Override
    public void insereAddress(AddressDTO endereco) throws StoreException {
        enderecoData.insereAddress(endereco);
    }

    @Override
    public List<AddressDTO> listaAddresssPorUserId(Integer usuarioId) throws StoreException {
        return enderecoData.listaAddresssPorUserId(usuarioId);
    }

    @Override
    public Boolean verificaAddressDoUser(Integer id, Integer usuarioId) throws StoreException {
        return enderecoData.verificaAddressDoUser(id, usuarioId);
    }
}
