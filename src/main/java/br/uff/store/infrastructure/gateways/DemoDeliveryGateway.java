package br.uff.store.infrastructure.gateways;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.DeliveryResultDTO;
import br.uff.store.core.interfaces.gateways.IDeliveryGateway;

/** The only delivery adapter until a carrier is chosen: nothing is shipped and no tracking code exists. */
public class DemoDeliveryGateway implements IDeliveryGateway {
    @Override
    public DeliveryResultDTO arrange(Integer userId, AddressDTO destination) {
        return new DeliveryResultDTO(DemoPaymentGateway.PROVIDER, "NONE", null, DeliveryResultDTO.NOT_ARRANGED, null);
    }
}
