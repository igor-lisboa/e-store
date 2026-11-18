package br.uff.store.core.dtos;

import java.io.Serializable;

import com.google.gson.Gson;

import br.uff.store.core.interfaces.dtos.IBaseDTO;

public class BaseDTO implements IBaseDTO, Serializable {

    @Override
    public String toJson() {
        return new Gson().toJson(this);
    }

}
