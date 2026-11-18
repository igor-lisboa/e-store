package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.RoleDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IRoleData {

    public List<RoleDTO> listRoles() throws StoreException;
}
