package br.uff.store.infrastructure.gateways;

import br.uff.store.core.dtos.PaymentResultDTO;
import br.uff.store.core.interfaces.gateways.IPaymentGateway;
import java.math.BigDecimal;

/** The only payment adapter until a vendor is chosen: records an order and never charges money. */
public class DemoPaymentGateway implements IPaymentGateway {
    public static final String PROVIDER = "demo";

    @Override
    public PaymentResultDTO authorize(Integer userId, BigDecimal amount) {
        return new PaymentResultDTO(PROVIDER, null, PaymentResultDTO.NOT_CHARGED, null);
    }

    @Override
    public void release(PaymentResultDTO payment) {
        // Nothing was reserved, so there is nothing to undo.
    }
}
