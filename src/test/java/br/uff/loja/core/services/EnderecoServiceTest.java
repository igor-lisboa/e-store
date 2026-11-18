package br.uff.loja.core.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.Assert.fail;

import br.uff.loja.core.dtos.EnderecoDTO;
import br.uff.loja.core.exceptions.LojaException;
import br.uff.loja.core.interfaces.data.IEnderecoData;
import org.junit.Before;
import org.junit.Test;

public class EnderecoServiceTest {
    private IEnderecoData addressData;
    private EnderecoService service;

    @Before
    public void setUp() {
        addressData = mock(IEnderecoData.class);
        service = new EnderecoService(addressData);
    }

    @Test
    public void rejectsAddressUpdatesByAnotherAccount() throws Exception {
        when(addressData.verificaEnderecoDoUsuario(7, 12)).thenReturn(false);

        try {
            service.atualizaEnderecoPorId(7, addressFor(12));
            fail("Expected an ownership error.");
        } catch (LojaException expected) {
            verify(addressData, never()).atualizaEnderecoPorId(eq(7), any(EnderecoDTO.class));
        }
    }

    @Test
    public void rejectsAddressDeletionByAnotherAccount() throws Exception {
        when(addressData.verificaEnderecoDoUsuario(7, 12)).thenReturn(false);

        try {
            service.excluiEnderecoPorId(7, 12);
            fail("Expected an ownership error.");
        } catch (LojaException expected) {
            verify(addressData, never()).excluiEnderecoPorId(7);
        }
    }

    @Test
    public void doesNotDeleteAnAddressThatWasUsedForAPurchase() throws Exception {
        when(addressData.verificaEnderecoDoUsuario(7, 12)).thenReturn(true);
        when(addressData.enderecoFoiUsadoEmAlgumaVenda(7)).thenReturn(true);

        try {
            service.excluiEnderecoPorId(7, 12);
            fail("Expected a purchase history error.");
        } catch (LojaException expected) {
            verify(addressData, never()).excluiEnderecoPorId(7);
        }
    }

    private EnderecoDTO addressFor(Integer userId) {
        return new EnderecoDTO("Home", userId, 12345, "Main Street", "City", "State", "Country");
    }
}
