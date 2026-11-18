package br.uff.store.core.dtos;

import java.util.Date;
import java.util.List;
import java.util.Map;

import br.uff.store.infrastructure.shared.Helper;

public class SaleDTO extends BaseDTO {

    private Integer id;
    private Integer carrinhoId;
    private Double precoTotal;
    private Date criadoEm;
    private Integer enderecoId;
    private Integer usuarioId;
    private List<CartItemDTO> produtosDoCart;
    private AddressDTO endereco;

    public SaleDTO() {
    }

    public SaleDTO(Map<String, Object> venda) {
        this.setId(Integer.valueOf(String.valueOf(venda.get("id"))));
        this.setCartId(Integer.valueOf(String.valueOf(venda.get("carrinhoId"))));
        this.setPrecoTotal(Double.valueOf(String.valueOf(venda.get("precoTotal"))));
        this.setCriadoEm((new Helper()).convertStringToDate("yyyy-MM-dd HH:mm:ss", String.valueOf(venda.get("criadoEm"))));
        this.setAddressId(Integer.valueOf(String.valueOf(venda.get("enderecoId"))));
        this.setUserId(Integer.valueOf(String.valueOf(venda.get("usuarioId"))));
    }

    public AddressDTO getAddress() {
        return endereco;
    }

    public void setAddress(AddressDTO endereco) {
        this.endereco = endereco;
    }

    public void setProductsDoCart(List<CartItemDTO> produtosDoCart) {
        this.produtosDoCart = produtosDoCart;
    }

    public List<CartItemDTO> getProductsDoCart() {
        return produtosDoCart;
    }

    public Integer getCartId() {
        return carrinhoId;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }

    public Integer getAddressId() {
        return enderecoId;
    }

    public Integer getId() {
        return id;
    }

    public Double getPrecoTotal() {
        return precoTotal;
    }

    public Integer getUserId() {
        return usuarioId;
    }

    public void setCartId(Integer carrinhoId) {
        this.carrinhoId = carrinhoId;
    }

    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }

    public void setAddressId(Integer enderecoId) {
        this.enderecoId = enderecoId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setPrecoTotal(Double precoTotal) {
        this.precoTotal = precoTotal;
    }

    public void setUserId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
}
