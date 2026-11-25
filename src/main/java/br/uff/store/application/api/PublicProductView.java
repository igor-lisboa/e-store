package br.uff.store.application.api;

import br.uff.store.core.dtos.ProductDTO;
import java.math.BigDecimal;

/** Product fields safe for anonymous and customer callers: availability only, never the stock count. */
final class PublicProductView {
    private final Integer id;
    private final String name;
    private final BigDecimal price;
    private final String description;
    private final String image;
    private final Integer categoryId;
    private final String category;
    private final boolean available;

    PublicProductView(ProductDTO product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.description = product.getDescription();
        this.image = product.getImage();
        this.categoryId = product.getCategoryId();
        this.category = product.getCategory();
        this.available = product.getQuantity() != null && product.getQuantity() > 0;
    }
}
