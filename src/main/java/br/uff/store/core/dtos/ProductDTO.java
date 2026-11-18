package br.uff.store.core.dtos;

import java.util.Map;
import com.google.gson.annotations.SerializedName;

public class ProductDTO extends BaseDTO {

    private Integer id;
    @SerializedName("name")
    private String nome;
    @SerializedName("price")
    private Double preco;
    @SerializedName("description")
    private String descricao;
    @SerializedName("image")
    private String imagem;
    @SerializedName("categoryId")
    private Integer categoriaId;
    @SerializedName("quantity")
    private Integer quantidade;
    @SerializedName("category")
    private String categoria;

    public void setCategory(String categoria) {
        this.categoria = categoria;
    }

    public String getCategory() {
        return categoria;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getImagem() {
        return imagem;
    }

    public void setCategoryId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Integer getCategoryId() {
        return categoriaId;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Double getPreco() {
        return preco;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public ProductDTO() {
        this.setNome("");
        this.setPreco(0.0);
        this.setDescricao("");
    }

    public ProductDTO(Map<String, Object> produto) {
        this.setId(Integer.valueOf(String.valueOf(produto.get("id"))));
        this.setNome(String.valueOf(produto.get("nome")));
        this.setPreco(Double.valueOf(String.valueOf(produto.get("preco"))));
        this.setDescricao(String.valueOf(produto.get("descricao")));
        this.setImagem(String.valueOf(produto.get("imagem")));
        this.setCategory(String.valueOf(produto.get("categoria")));
        this.setCategoryId(Integer.valueOf(String.valueOf(produto.get("categoriaId"))));
        this.setQuantidade(Integer.valueOf(String.valueOf(produto.get("quantidade"))));
    }

    public ProductDTO(Integer id, String nome, Double preco, String descricao, String imagem, Integer categoriaId, Integer quantidade) {
        this.setId(id);
        this.setNome(nome);
        this.setPreco(preco);
        this.setDescricao(descricao);
        this.setImagem(imagem);
        this.setCategoryId(categoriaId);
        this.setQuantidade(quantidade);
    }
}
