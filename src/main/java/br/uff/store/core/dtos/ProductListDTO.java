package br.uff.store.core.dtos;

import java.util.Map;

public class ProductListDTO extends BaseDTO {

    private Integer id;
    private String nome;
    private Double preco;
    private String imagem;
    private String categoria;

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getImagem() {
        return imagem;
    }

    public void setCategory(String categoria) {
        this.categoria = categoria;
    }

    public String getCategory() {
        return categoria;
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

    public ProductListDTO() {
    }

    public ProductListDTO(Map<String, Object> produto) {
        this.setId(Integer.valueOf(String.valueOf(produto.get("id"))));
        this.setNome(String.valueOf(produto.get("nome")));
        this.setPreco(Double.valueOf(String.valueOf(produto.get("preco"))));
        this.setImagem(String.valueOf(produto.get("imagem")));
        this.setCategory(String.valueOf(produto.get("categoria")));
    }

    public ProductListDTO(
            Integer id,
            String nome,
            Double preco,
            String imagem,
            String categoria
    ) {
        this.setId(id);
        this.setNome(nome);
        this.setPreco(preco);
        this.setImagem(imagem);
        this.setCategory(categoria);
    }
}
