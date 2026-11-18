package br.uff.store.steps.stepDefinition;

import java.util.List;
import java.util.Random;

import org.junit.Assert;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.enums.ProductCategory;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.services.ProductService;
import io.cucumber.java.es.Dado;
import io.cucumber.java.it.Quando;
import io.cucumber.java.pt.Entao;

public class ProductStepDefinition {
    private String nome;
    private Double preco;
    private String descricao;
    private String imagem;
    private String categoria;
    private Integer idProductJaCadastrado = null;

    @Dado("os dados do produto {string}, {double}, {string}, {string} e {string} a ser cadastrado")
    public void os_dados_do_produto_e_a_ser_cadastrado(String string, Double double1, String string2, String string3,
            String string4) {
        this.nome = string;
        this.preco = double1;
        this.descricao = string2;
        this.imagem = string3;
        this.categoria = string4;
        this.idProductJaCadastrado = null;
    }

    @Quando("usuario tenta o insert do produto verificando se o mesmo ja nao esta cadastrado e inclusao de alguma quantidade em estoque")
    public void usuario_tenta_o_insert_do_produto_verificando_se_o_mesmo_ja_nao_esta_cadastrado_e_inclusao_de_alguma_quantidade_em_estoque()
            throws StoreException {
        IProductService produtoService = new ProductService();
        List<ProductDTO> produtos = produtoService.listaProductsAdm();

        Integer categoriaId = ProductCategory.valueOf(this.categoria).getId();

        produtos.forEach(produto -> {
            if (produto.getCategoryId().equals(categoriaId) && produto.getNome().equals(this.nome)) {
                this.idProductJaCadastrado = produto.getId();
            }
        });

        if (this.idProductJaCadastrado == null) {
            produtoService.insereProduct(new ProductDTO(this.idProductJaCadastrado, this.nome, this.preco,
                    this.descricao, this.imagem, categoriaId, 0));

            produtos = produtoService.listaProductsAdm();

            produtos.forEach(produto -> {
                if (produto.getCategoryId().equals(categoriaId) && produto.getNome().equals(this.nome)) {
                    this.idProductJaCadastrado = produto.getId();
                }
            });
        }

        if (this.idProductJaCadastrado == null) {
            throw new StoreException("Product não cadastrado!");
        }

        produtoService.insereQuantidadeEmEstoqueDoProductPorId(this.idProductJaCadastrado,
                new Random().nextInt(1000) + 1);
    }

    @Entao("a quantidade em estoque desse produto sera maior que {int}")
    public void a_quantidade_em_estoque_desse_produto_sera_maior_que(Integer int1) throws StoreException {
        IProductService produtoService = new ProductService();

        Integer usuarioId = null;
        Integer qtd = produtoService.mostraProductVitrineParaUser(this.idProductJaCadastrado, usuarioId)
                .getQuantidade();

        Assert.assertEquals(true, Boolean.TRUE.equals(qtd > int1));
    }
}
