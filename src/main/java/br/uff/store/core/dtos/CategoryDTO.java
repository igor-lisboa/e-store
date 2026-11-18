package br.uff.store.core.dtos;

import java.util.Map;
import com.google.gson.annotations.SerializedName;

public class CategoryDTO {

    private Integer id;
    @SerializedName("name")
    private String nome;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public CategoryDTO() {
    }

    public CategoryDTO(Map<String, Object> categoria) {
        this.setId(Integer.valueOf(String.valueOf(categoria.get("id"))));
        this.setNome(String.valueOf(categoria.get("nome")));
    }
}
