package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICategoryData;
import br.uff.store.core.interfaces.services.ICategoryService;
import br.uff.store.infrastructure.data.CategoryData;

public class CategoryService implements ICategoryService {

    private ICategoryData categoriaData;

    public CategoryService() {
        categoriaData = new CategoryData();
    }

    @Override
    public List<CategoryDTO> listaCategorys() throws StoreException {
        return categoriaData.listaCategorys();
    }
}
