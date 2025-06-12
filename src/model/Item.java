package model;

public class Item {

    private int codItem;
    private String descricao; // Nome do item
    private double valor;
    private int quantidade;
    private Categoria categoria; // Vincula o item a uma categoria

    // Construtor
    public Item(int codItem, String descricao, double valor, int quantidade, Categoria categoria) {
        this.codItem = codItem;
        this.descricao = descricao;
        this.valor = valor;
        this.quantidade = quantidade;
        this.categoria = categoria;
    }

    // Getters e Setters
    public int getCodItem() { return codItem; }
    public void setCodItem(int codItem) { this.codItem = codItem; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    
    public boolean deduzirEstoque(int quantidade) {
        if (this.quantidade >= quantidade) {
            this.quantidade -= quantidade;
            return true;
        }
        return false; // Estoque insuficiente
    }

    public void adicionarEstoque(int quantidade) {
        this.quantidade += quantidade;
    }
    @Override
    
    public String toString(){
    	return codItem + " - " + descricao + " (R$" + String.format("%.2f", valor) + " | Qtd: " + quantidade + ") [" + (categoria != null ? categoria.getDescricao() : "N/A") + "]";
    }
}