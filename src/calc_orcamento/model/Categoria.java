package calc_orcamento.model;

public class Categoria {

    private int codCategoria;
    private String descricao;

    // Construtor
    public Categoria(int codCategoria, String descricao) {
        this.codCategoria = codCategoria;
        this.descricao = descricao;
    }

    // Getters e Setters
    public int getCodCategoria() {
        return codCategoria;
    }

    public void setCodCategoria(int codCategoria) {
        this.codCategoria = codCategoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
