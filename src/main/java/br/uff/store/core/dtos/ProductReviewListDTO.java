package br.uff.store.core.dtos;

import java.util.Map;

public class ProductReviewListDTO extends BaseDTO {

    private String avaliador;
    private String avaliacaoTitulo;
    private String avaliacao;
    private String avaliacaoDescricao;
    private String avaliacaoData;
    private String avaliacaoDataSimples;

    public ProductReviewListDTO() {
    }

    public ProductReviewListDTO(Map<String, Object> avaliacao) {
        this.setAvaliador(String.valueOf(avaliacao.get("avaliador")));
        this.setReviewTitulo(String.valueOf(avaliacao.get("avaliacaoTitulo")));
        this.setReview(String.valueOf(avaliacao.get("avaliacao")));
        this.setReviewDescricao(String.valueOf(avaliacao.get("avaliacaoDescricao")));
        this.setReviewData(String.valueOf(avaliacao.get("avaliacaoData")));
        this.setReviewDataSimples(String.valueOf(avaliacao.get("avaliacaoDataSimples")));
    }

    public void setReviewDataSimples(String avaliacaoDataSimples) {
        this.avaliacaoDataSimples = avaliacaoDataSimples;
    }

    public String getReviewDataSimples() {
        return avaliacaoDataSimples;
    }

    public void setReviewData(String avaliacaoData) {
        this.avaliacaoData = avaliacaoData;
    }

    public String getReviewData() {
        return avaliacaoData;
    }

    public String getReviewDescricao() {
        return avaliacaoDescricao;
    }

    public void setReviewDescricao(String avaliacaoDescricao) {
        this.avaliacaoDescricao = avaliacaoDescricao;
    }

    public void setReview(String avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getReview() {
        return avaliacao;
    }

    public void setReviewTitulo(String avaliacaoTitulo) {
        this.avaliacaoTitulo = avaliacaoTitulo;
    }

    public String getReviewTitulo() {
        return avaliacaoTitulo;
    }

    public void setAvaliador(String avaliador) {
        this.avaliador = avaliador;
    }

    public String getAvaliador() {
        return avaliador;
    }
}
