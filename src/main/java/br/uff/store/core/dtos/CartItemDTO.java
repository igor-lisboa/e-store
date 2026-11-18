package br.uff.store.core.dtos;

import java.util.Map;

public class CartItemDTO extends BaseDTO {

    private Integer id;
    private Integer produtoId;
    private String nome;
    private String descricao;
    private String imagem;
    private Double preco;
    private Integer quantidadeEmEstoque;
    private Integer quantidade;
    private Double precoTotal;

    public CartItemDTO() {
    }

    public CartItemDTO(Map<String, Object> carrinhoProduct) {
        this.setId(Integer.valueOf(String.valueOf(carrinhoProduct.get("id"))));
        this.setProductId(Integer.valueOf(String.valueOf(carrinhoProduct.get("produtoId"))));
        this.setNome(String.valueOf(carrinhoProduct.get("nome")));
        this.setDescricao(String.valueOf(carrinhoProduct.get("descricao")));
        this.setImagem(String.valueOf(carrinhoProduct.get("imagem")));
        this.setPreco(Double.valueOf(String.valueOf(carrinhoProduct.get("preco"))));
        this.setQuantidadeEmEstoque(Integer.valueOf(String.valueOf(carrinhoProduct.get("quantidadeEmEstoque"))));
        this.setQuantidade(Integer.valueOf(String.valueOf(carrinhoProduct.get("quantidade"))));
        this.setPrecoTotal(Double.valueOf(String.valueOf(carrinhoProduct.get("precoTotal"))));
    }

    public void setProductId(Integer produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getProductId() {
        return produtoId;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getId() {
        return id;
    }

    public String getImagem() {
        return imagem;
    }

    public String getNome() {
        return nome;
    }

    public Double getPreco() {
        return preco;
    }

    public Double getPrecoTotal() {
        return precoTotal;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public Integer getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public void setPrecoTotal(Double precoTotal) {
        this.precoTotal = precoTotal;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public void setQuantidadeEmEstoque(Integer quantidadeEmEstoque) {
        this.quantidadeEmEstoque = quantidadeEmEstoque;
    }
}
