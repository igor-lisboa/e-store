package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.RoleDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IRoleService {

    public List<RoleDTO> listaRoles() throws StoreException;
}
