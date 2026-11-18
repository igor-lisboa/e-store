package br.uff.store.core.dtos;

public class ProductReviewInputDTO extends BaseDTO {

    private Integer usuarioId;
    private Integer produtoId;
    private Integer avaliacao;
    private String descricao;
    private String titulo;

    public ProductReviewInputDTO() {
    }

    public ProductReviewInputDTO(
            Integer usuarioId, Integer produtoId, Integer avaliacao, String descricao, String titulo
    ) {
        this.setUserId(usuarioId);
        this.setProductId(produtoId);
        this.setReview(avaliacao);
        this.setDescricao(descricao);
        this.setTitulo(titulo);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getProductId() {
        return produtoId;
    }

    public void setProductId(Integer produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getReview() {
        return avaliacao;
    }

    public void setReview(Integer avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Integer getUserId() {
        return usuarioId;
    }

    public void setUserId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
}
