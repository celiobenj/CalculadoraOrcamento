package model;

import java.util.Date;

public class Orcamento {

    private int codOrcamento;
    private Date data;
    private double valorTotal;
    private static final double IMPOSTO = 0.065; // 6,5% de imposto

    // Construtor
    public Orcamento(int codOrcamento) {
        this.codOrcamento = codOrcamento;
        this.data = new Date(); // Define a data de criação automaticamente
        this.valorTotal = 0.0;
    }

    // Getters e Setters
    public int getCodOrcamento() { return codOrcamento; }
    public void setCodOrcamento(int codOrcamento) { this.codOrcamento = codOrcamento; }
    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }
    public double getValorTotal() { return valorTotal; }
    // Método para calcular o valor final com imposto
    public double getValorFinalComImposto() { return this.valorTotal * (1 + IMPOSTO); }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    // Método auxiliar para adicionar custos ao total
    public void adicionarCusto(double custo) { this.valorTotal += custo; }
    
    @Override
    
    public String toString() {
    	return "ORC-" + codOrcamento;
    }
}
