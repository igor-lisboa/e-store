package br.uff.store.core.interfaces.gateways;

import br.uff.store.core.dtos.PaymentResultDTO;
import br.uff.store.core.exceptions.StoreException;
import java.math.BigDecimal;

/** Port for taking payment. Each vendor is an adapter; core never names a vendor. */
public interface IPaymentGateway {

    /** Reserves or takes the amount. Throws when the payment is declined, so no order is created. */
    PaymentResultDTO authorize(Integer userId, BigDecimal amount) throws StoreException;

    /** Undoes an authorization when the order could not be completed afterwards. */
    void release(PaymentResultDTO payment) throws StoreException;
}
