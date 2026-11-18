package br.uff.loja.core.services;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.loja.core.dtos.AvaliacaoProdutoInsertDTO;
import br.uff.loja.core.exceptions.LojaException;
import br.uff.loja.core.interfaces.data.IAvaliacaoProdutoData;
import org.junit.Before;
import org.junit.Test;

public class AvaliacaoServiceTest {
    private IAvaliacaoProdutoData reviewData;
    private AvaliacaoService service;

    @Before
    public void setUp() {
        reviewData = mock(IAvaliacaoProdutoData.class);
        service = new AvaliacaoService(reviewData);
    }

    @Test
    public void acceptsRatingsFromOneToFive() throws Exception {
        AvaliacaoProdutoInsertDTO review = new AvaliacaoProdutoInsertDTO(4, 7, 5, "Review", "Title");
        when(reviewData.jaFoiAvaliado(4, 7)).thenReturn(false);

        service.avaliaProduto(review);

        verify(reviewData).insereAvaliacaoDoProduto(review);
    }

    @Test
    public void rejectsRatingsOutsideTheSupportedRange() throws Exception {
        assertInvalid(new AvaliacaoProdutoInsertDTO(4, 7, 0, "Review", "Title"));
        assertInvalid(new AvaliacaoProdutoInsertDTO(4, 7, 6, "Review", "Title"));
    }

    @Test
    public void rejectsMissingUsersAndBlankTitles() throws Exception {
        assertInvalid(new AvaliacaoProdutoInsertDTO(null, 7, 4, "Review", "Title"));
        assertInvalid(new AvaliacaoProdutoInsertDTO(4, 7, 4, "Review", "  "));
    }

    private void assertInvalid(AvaliacaoProdutoInsertDTO review) throws Exception {
        try {
            service.avaliaProduto(review);
            fail("Expected invalid review data to be rejected.");
        } catch (LojaException expected) {
            verify(reviewData, never()).insereAvaliacaoDoProduto(review);
        }
    }
}
