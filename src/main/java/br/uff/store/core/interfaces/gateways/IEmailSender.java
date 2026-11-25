package br.uff.store.core.interfaces.gateways;

import br.uff.store.core.exceptions.StoreException;

/** Port for sending email. A real provider (SMTP, a vendor API) is an adapter; core never names one. */
public interface IEmailSender {
    void send(String to, String subject, String body) throws StoreException;
}
