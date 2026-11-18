package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICategoryData;
import br.uff.store.core.interfaces.services.ICategoryService;

public class CategoryService implements ICategoryService {

    private final ICategoryData categoryData;

    public CategoryService(ICategoryData data) {
        categoryData = java.util.Objects.requireNonNull(data, "Repository is required.");
    }

    @Override
    public List<CategoryDTO> categories() throws StoreException {
        return categoryData.categories();
    }
}
