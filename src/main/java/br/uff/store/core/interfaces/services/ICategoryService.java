package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ICategoryService {

    public List<CategoryDTO> categories() throws StoreException;
}
