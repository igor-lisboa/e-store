package br.uff.store.core.interfaces.shared;

import java.util.Date;

import br.uff.store.core.exceptions.StoreException;

public interface IHelper {

    public Date convertStringToDate(String format, String date) throws StoreException;
}
