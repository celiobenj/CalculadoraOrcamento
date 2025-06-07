package calc_orcamento.model;

public class ItemOrcamentado {

    private int codItemOrcamentado;
    private Orcamento orcamento; // Orçamento ao qual o item pertence
    private Item item; // O item do catálogo que está sendo orçado
    private int quantidadeUtilizada;
    private double valorItemTotal;

    // Construtor
    public ItemOrcamentado(int codItemOrcamentado, Orcamento orcamento, Item item, int quantidadeUtilizada) {
        this.codItemOrcamentado = codItemOrcamentado;
        this.orcamento = orcamento;
        this.item = item;
        this.quantidadeUtilizada = quantidadeUtilizada;
        this.valorItemTotal = item.getValor() * quantidadeUtilizada; // Calcula o subtotal
    }

    // Getters e Setters
    public int getCodItemOrcamentado() {
        return codItemOrcamentado;
    }

    public void setCodItemOrcamentado(int codItemOrcamentado) {
        this.codItemOrcamentado = codItemOrcamentado;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantidadeUtilizada() {
        return quantidadeUtilizada;
    }

    public void setQuantidadeUtilizada(int quantidadeUtilizada) {
        this.quantidadeUtilizada = quantidadeUtilizada;
        this.valorItemTotal = this.item.getValor() * quantidadeUtilizada; // Recalcula ao alterar a quantidade
    }

    public double getValorItemTotal() {
        return valorItemTotal;
    }
}
