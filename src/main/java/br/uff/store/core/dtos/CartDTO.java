package br.uff.store.core.dtos;

import java.util.Date;
import java.util.Map;

import br.uff.store.infrastructure.shared.Helper;

public class CartDTO extends BaseDTO {

    private Integer id;
    private String ip;
    private Integer usuarioId;
    private Date criadoEm;

    public CartDTO() {
    }

    public CartDTO(Map<String, Object> carrinho) {
        this.setId(Integer.valueOf(String.valueOf(carrinho.get("id"))));
        this.setIp(String.valueOf(carrinho.get("ip")));
        if (carrinho.get("usuarioId") == null) {
            this.setUserId(null);
        } else {
            this.setUserId(Integer.valueOf(String.valueOf(carrinho.get("usuarioId"))));
        }
        this.setCriadoEm(new Helper().convertStringToDate("yyyy-MM-dd HH:mm:ss", String.valueOf(carrinho.get("criadoEm"))));
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUserId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public Integer getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public Integer getUserId() {
        return usuarioId;
    }
}
