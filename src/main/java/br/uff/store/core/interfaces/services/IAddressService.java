package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IAddressService {

    public AddressDTO encontraAddressPorId(Integer id) throws StoreException;

    public void excluiAddressPorId(Integer id, Integer usuarioId) throws StoreException;

    public AddressDTO atualizaAddressPorId(Integer id, AddressDTO endereco) throws StoreException;

    public void insereAddress(AddressDTO endereco) throws StoreException;

    public List<AddressDTO> listaAddresssPorUserId(Integer usuarioId) throws StoreException;

    public Boolean verificaAddressDoUser(Integer id, Integer usuarioId) throws StoreException;
}
