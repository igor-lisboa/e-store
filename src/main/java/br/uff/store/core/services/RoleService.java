package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.RoleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IRoleData;
import br.uff.store.core.interfaces.services.IRoleService;
import br.uff.store.infrastructure.data.RoleData;

public class RoleService implements IRoleService {

    private IRoleData roleData;

    public RoleService() {
        roleData = new RoleData();
    }

    @Override
    public List<RoleDTO> listaRoles() throws StoreException {
        return roleData.listaRoles();
    }

}
