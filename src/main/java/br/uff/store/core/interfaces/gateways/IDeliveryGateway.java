package br.uff.store.core.interfaces.gateways;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.DeliveryResultDTO;
import br.uff.store.core.exceptions.StoreException;

/** Port for arranging delivery. Each carrier is an adapter; core never names a vendor. */
public interface IDeliveryGateway {

    DeliveryResultDTO arrange(Integer userId, AddressDTO destination) throws StoreException;
}
