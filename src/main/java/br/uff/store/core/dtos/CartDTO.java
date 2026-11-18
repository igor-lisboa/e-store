package br.uff.store.core.dtos;

import java.util.Date;
import java.util.Map;


public class CartDTO extends BaseDTO {

    private Integer id;
    private String ip;
    private Integer userId;
    private Date createdAt;

    public CartDTO() {
    }

    public CartDTO(Map<String, Object> cart) {
        this.setId(Integer.valueOf(String.valueOf(cart.get("id"))));
        this.setIp(String.valueOf(cart.get("ip")));
        if (cart.get("userId") == null) {
            this.setUserId(null);
        } else {
            this.setUserId(Integer.valueOf(String.valueOf(cart.get("userId"))));
        }
        this.setCreatedAt(DtoValues.date(cart.get("createdAt")));
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Integer getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public Integer getUserId() {
        return userId;
    }
}
