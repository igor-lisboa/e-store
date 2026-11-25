package br.uff.store.core.services;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductData;
import java.math.BigDecimal;
import org.junit.Test;

/** Hostile or oversized administrator input must be rejected before it reaches persistence. */
public class ProductBoundaryTest {
    private final IProductData productData = mock(IProductData.class);
    private final ProductService service = new ProductService(productData);

    private ProductDTO productWithImage(String image) {
        return new ProductDTO(null, "Console", new BigDecimal("10.00"), "Description", image, 5, 0);
    }

    @Test
    public void imageSourcesThatCouldRunScriptOrReadLocalFilesAreRejected() throws Exception {
        String[] hostile = {"javascript:alert(1)", "JaVaScRiPt:alert(1)", "file:///etc/passwd", "//evil.example/x.png",
            "data:text/html;base64,PHNjcmlwdD4=", "vbscript:msgbox(1)", "../../secret.png", "  ", "ftp://host/x.png"};
        for (String image : hostile) {
            try {
                service.insertProduct(productWithImage(image));
                fail("Hostile image source accepted: " + image);
            } catch (StoreException expected) {
                // Expected.
            }
        }
        verify(productData, never()).insertProduct(any());
    }

    @Test
    public void imageSourcesProducedByTheAdminFormAndSeedsAreAccepted() throws Exception {
        String[] allowed = {"data:image/png;base64,iVBORw0KGgo=", "img/ps4.jpg", "https://cdn.example/x.png",
            "http://cdn.example/x.png", "DATA:IMAGE/JPEG;base64,/9j/4AAQ"};
        for (String image : allowed) {
            service.insertProduct(productWithImage(image));
        }
        verify(productData, org.mockito.Mockito.times(allowed.length)).insertProduct(any());
    }

    @Test
    public void updatesApplyTheSameImageRule() throws Exception {
        try {
            service.updateProductById(3, productWithImage("javascript:alert(1)"));
            fail("Updates must not bypass the image rule.");
        } catch (StoreException expected) {
            verify(productData, never()).updateProductById(anyInt(), any());
        }
    }

    @Test
    public void stockIncrementsBeyondTheOperationalLimitAreRejected() throws Exception {
        service.addProductStock(3, 1000000);
        verify(productData).addProductStock(3, 1000000);

        for (int increment : new int[] {1000001, Integer.MAX_VALUE, 0, -5}) {
            try {
                service.addProductStock(3, increment);
                fail("Increment accepted: " + increment);
            } catch (StoreException expected) {
                // Expected.
            }
        }
        verify(productData, org.mockito.Mockito.times(1)).addProductStock(anyInt(), anyInt());
    }
}
