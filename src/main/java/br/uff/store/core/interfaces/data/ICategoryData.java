package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ICategoryData {

    public List<CategoryDTO> categories() throws StoreException;
}
