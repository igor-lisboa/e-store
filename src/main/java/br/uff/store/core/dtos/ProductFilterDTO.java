package br.uff.store.core.dtos;

import java.util.List;

public class ProductFilterDTO extends BaseDTO {

    private Integer paginaAtual;
    private Integer itensPorPagina;
    private String pesquisa;
    private Double precoMinimo;
    private Double precoMaximo;
    private Boolean apenasFavoritados;
    private Integer usuarioId;
    private Boolean apenasLancamentos;
    private List<String> categorias;
    private List<String> subCategorys;

    public Integer getItensPorPagina() {
        return itensPorPagina;
    }

    public void setItensPorPagina(Integer itensPorPagina) {
        this.itensPorPagina = itensPorPagina;
    }

    public Boolean getApenasFavoritados() {
        return apenasFavoritados;
    }

    public Boolean getApenasLancamentos() {
        return apenasLancamentos;
    }

    public List<String> getCategorys() {
        return categorias;
    }

    public Integer getPaginaAtual() {
        return paginaAtual;
    }

    public String getPesquisa() {
        return pesquisa;
    }

    public Double getPrecoMaximo() {
        return precoMaximo;
    }

    public Double getPrecoMinimo() {
        return precoMinimo;
    }

    public List<String> getSubCategorys() {
        return subCategorys;
    }

    public Integer getUserId() {
        return usuarioId;
    }

    public void setApenasFavoritados(Boolean apenasFavoritados) {
        this.apenasFavoritados = apenasFavoritados;
    }

    public void setApenasLancamentos(Boolean apenasLancamentos) {
        this.apenasLancamentos = apenasLancamentos;
    }

    public void setCategorys(List<String> categorias) {
        this.categorias = categorias;
    }

    public void setPaginaAtual(Integer paginaAtual) {
        this.paginaAtual = paginaAtual;
    }

    public void setPesquisa(String pesquisa) {
        this.pesquisa = pesquisa;
    }

    public void setPrecoMaximo(Double precoMaximo) {
        this.precoMaximo = precoMaximo;
    }

    public void setPrecoMinimo(Double precoMinimo) {
        this.precoMinimo = precoMinimo;
    }

    public void setSubCategorys(List<String> subCategorys) {
        this.subCategorys = subCategorys;
    }

    public void setUserId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }
}
