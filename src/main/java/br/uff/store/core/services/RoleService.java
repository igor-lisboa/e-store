package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.RoleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IRoleData;
import br.uff.store.core.interfaces.services.IRoleService;

public class RoleService implements IRoleService {

    private final IRoleData roleData;

    public RoleService(IRoleData data) {
        roleData = java.util.Objects.requireNonNull(data, "Repository is required.");
    }

    @Override
    public List<RoleDTO> listRoles() throws StoreException {
        return roleData.listRoles();
    }

}
