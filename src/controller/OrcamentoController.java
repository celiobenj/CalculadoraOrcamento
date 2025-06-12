package controller;

import model.Categoria;
import model.Item;
import model.Orcamento;
import model.ItemOrcamentado;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrcamentoController {
    private List<Categoria> categorias = new ArrayList<>();
    private List<Item> itens = new ArrayList<>();
    private List<Orcamento> orcamentos = new ArrayList<>();
    private List<ItemOrcamentado> itensOrcamentados = new ArrayList<>();

    public void setCategoria(Categoria categoria) {
        this.categorias.add(categoria);
        System.out.println("Categoria saved (in-memory): " + categoria.getDescricao()); // não com print
    }

    public List<Categoria> getCategorias() {
        return new ArrayList<>(this.categorias);
    }

    public boolean setItem(Item item) { // Para adicionar um NOVO item ao catálogo
        if (item == null || item.getDescricao().trim().isEmpty() || item.getValor() <= 0 || item.getQuantidade() < 0 || item.getCodItem() <= 0) {
            System.err.println("Erro: Dados do item inválidos.");
            return false;
        }
        boolean exists = this.itens.stream()
                .anyMatch(it -> it.getCodItem() == item.getCodItem() || it.getDescricao().equalsIgnoreCase(item.getDescricao()));
        if (exists) {
            System.err.println("Erro: Item com este código ou descrição já existe.");
            return false;
        }
        this.itens.add(item);
        return true;
    }

    public boolean atualizarItem(Item itemAtualizado) { // Para atualizar um item existente no catálogo
        if (itemAtualizado == null || itemAtualizado.getDescricao().trim().isEmpty() || itemAtualizado.getValor() <= 0 || itemAtualizado.getQuantidade() < 0 || itemAtualizado.getCodItem() <= 0) {
            System.err.println("Erro: Dados do item para atualização inválidos.");
            return false;
        }
        boolean descDuplicadaParaOutroItem = this.itens.stream()
                .anyMatch(i -> i.getDescricao().equalsIgnoreCase(itemAtualizado.getDescricao()) && i.getCodItem() != itemAtualizado.getCodItem());
        if (descDuplicadaParaOutroItem) {
            System.err.println("Erro: Já existe outro item com esta descrição.");
            return false;
        }

        for (int i = 0; i < this.itens.size(); i++) {
            if (this.itens.get(i).getCodItem() == itemAtualizado.getCodItem()) {
                this.itens.set(i, itemAtualizado); // Atualiza o item (incluindo seu estoque)
                return true;
            }
        }
        System.err.println("Erro: Item com código " + itemAtualizado.getCodItem() + " não encontrado para atualização.");
        return false;
    }

    public boolean removerItem(int codItem) {
        boolean isInUse = this.itensOrcamentados.stream()
                .anyMatch(io -> io.getItem() != null && io.getItem().getCodItem() == codItem);
        if (isInUse) {
            System.err.println("Erro: Não é possível remover o item " + codItem + " pois ele está sendo utilizado em orçamentos.");
            return false;
        }
        boolean removed = this.itens.removeIf(item -> item.getCodItem() == codItem);
        if (!removed) {
            System.err.println("Erro: Item com código " + codItem + " não encontrado para remoção.");
        }
        return removed;
    }

    public List<Item> getItens() { return new ArrayList<>(this.itens); }

    public Optional<Item> buscarItemPorCodigo(int codItem) {
        return this.itens.stream()
                .filter(item -> item.getCodItem() == codItem)
                .findFirst();
    }


    // --- Métodos de Orçamento (Gerenciamento em memória) ---
    public boolean setOrcamento(Orcamento orcamento) { // Para adicionar um NOVO orçamento
        if (orcamento == null || orcamento.getCodOrcamento() <= 0) {
            System.err.println("Erro: Orçamento inválido.");
            return false;
        }
        boolean exists = this.orcamentos.stream()
                .anyMatch(o -> o.getCodOrcamento() == orcamento.getCodOrcamento());
        if (exists) {
            System.err.println("Erro: Orçamento com este código já existe.");
            return false;
        }
        this.orcamentos.add(orcamento);
        return true;
    }

    public boolean atualizarOrcamento(Orcamento orcamentoAtualizado) { // Para atualizar um orçamento existente
        if (orcamentoAtualizado == null || orcamentoAtualizado.getCodOrcamento() <= 0) {
            System.err.println("Erro: Orçamento para atualização inválido.");
            return false;
        }
        for (int i = 0; i < this.orcamentos.size(); i++) {
            if (this.orcamentos.get(i).getCodOrcamento() == orcamentoAtualizado.getCodOrcamento()) {
                this.orcamentos.set(i, orcamentoAtualizado);
                return true;
            }
        }
        System.err.println("Erro: Orçamento com código " + orcamentoAtualizado.getCodOrcamento() + " não encontrado para atualização.");
        return false;
    }
    
    public boolean removerOrcamento(int codOrcamento) {
        Optional<Orcamento> orcamentoParaRemoverOpt = this.orcamentos.stream()
                .filter(o -> o.getCodOrcamento() == codOrcamento)
                .findFirst();

        if (orcamentoParaRemoverOpt.isPresent()) {
            // Antes de remover o orçamento, devolve os itens orçados ao estoque
            removerTodosItensOrcamentadosDoOrcamento(codOrcamento);
            return this.orcamentos.remove(orcamentoParaRemoverOpt.get());
        }
        System.err.println("Erro: Orçamento com código " + codOrcamento + " não encontrado para remoção.");
        return false;
    }

    public List<Orcamento> getOrcamento() { return new ArrayList<>(this.orcamentos); }


    // --- Métodos de ItemOrcamentado (Com Lógica de Estoque CRÍTICA) ---
    public boolean adicionarItemOrcamentado(ItemOrcamentado itemOrcamentado) {
        if (itemOrcamentado == null || itemOrcamentado.getCodItemOrcamentado() <= 0 ||
            itemOrcamentado.getItem() == null || itemOrcamentado.getOrcamento() == null ||
            itemOrcamentado.getQuantidadeUtilizada() <= 0) {
            System.err.println("Erro: Item orçado inválido.");
            return false;
        }

        // 1. Verifica duplicidade de codItemOrcamentado
        boolean exists = this.itensOrcamentados.stream()
                .anyMatch(io -> io.getCodItemOrcamentado() == itemOrcamentado.getCodItemOrcamentado());
        if (exists) {
            System.err.println("Erro: Item orçado com este código já existe.");
            return false;
        }

        // 2. Busca o item no estoque e verifica disponibilidade
        Optional<Item> itemNoEstoqueOpt = buscarItemPorCodigo(itemOrcamentado.getItem().getCodItem());
        if (!itemNoEstoqueOpt.isPresent()) {
            System.err.println("Erro: Item (" + itemOrcamentado.getItem().getDescricao() + ") não encontrado no catálogo/estoque.");
            return false;
        }
        Item itemDoEstoque = itemNoEstoqueOpt.get();

        if (itemDoEstoque.getQuantidade() < itemOrcamentado.getQuantidadeUtilizada()) { // Usa getQuantidade() que é o estoque
            System.err.println("Erro: Estoque insuficiente para o item '" + itemDoEstoque.getDescricao() + "'. Disponível: " + itemDoEstoque.getQuantidade() + ", Solicitado: " + itemOrcamentado.getQuantidadeUtilizada());
            return false;
        }

        // 3. Deduz do estoque e adiciona o ItemOrcamentado
        if (itemDoEstoque.deduzirEstoque(itemOrcamentado.getQuantidadeUtilizada())) {
            this.itensOrcamentados.add(itemOrcamentado);
            return true;
        } else {
            System.err.println("Erro desconhecido ao deduzir estoque."); // Não deve acontecer
            return false;
        }
    }

    public boolean atualizarItemOrcamentado(ItemOrcamentado itemOrcamentadoAtualizado) {
        if (itemOrcamentadoAtualizado == null || itemOrcamentadoAtualizado.getCodItemOrcamentado() <= 0 ||
            itemOrcamentadoAtualizado.getItem() == null || itemOrcamentadoAtualizado.getOrcamento() == null ||
            itemOrcamentadoAtualizado.getQuantidadeUtilizada() <= 0) { // Quantidade utilizada > 0
            System.err.println("Erro: Item orçado para atualização inválido.");
            return false;
        }

        for (int i = 0; i < this.itensOrcamentados.size(); i++) {
            ItemOrcamentado existingItemOrcamentado = this.itensOrcamentados.get(i);

            if (existingItemOrcamentado.getCodItemOrcamentado() == itemOrcamentadoAtualizado.getCodItemOrcamentado()) {
                Item itemNoEstoque = buscarItemPorCodigo(itemOrcamentadoAtualizado.getItem().getCodItem()).orElse(null);
                if (itemNoEstoque == null) {
                    System.err.println("Erro: Item de catálogo não encontrado para o item orçado (cód: " + itemOrcamentadoAtualizado.getItem().getCodItem() + ").");
                    return false;
                }

                int oldQuantity = existingItemOrcamentado.getQuantidadeUtilizada();
                int newQuantity = itemOrcamentadoAtualizado.getQuantidadeUtilizada();
                int quantityDifference = newQuantity - oldQuantity;

                if (quantityDifference > 0) { // Quantidade utilizada aumentou
                    if (itemNoEstoque.getQuantidade() < quantityDifference) {
                        System.err.println("Erro: Estoque insuficiente para aumentar a quantidade do item '" + itemNoEstoque.getDescricao() + "'. Disponível: " + itemNoEstoque.getQuantidade() + ", Necessário: " + quantityDifference);
                        return false;
                    }
                    itemNoEstoque.deduzirEstoque(quantityDifference);
                } else if (quantityDifference < 0) { // Quantidade utilizada diminuiu
                    itemNoEstoque.adicionarEstoque(Math.abs(quantityDifference));
                }
                
                // Agora, atualiza o item orçado na lista
                this.itensOrcamentados.set(i, itemOrcamentadoAtualizado);
                return true;
            }
        }
        System.err.println("Erro: Item orçado com código " + itemOrcamentadoAtualizado.getCodItemOrcamentado() + " não encontrado para atualização.");
        return false;
    }

    public boolean removerItemOrcamentado(int codItemOrcamentado) {
        Optional<ItemOrcamentado> itemToRemoveOpt = this.itensOrcamentados.stream()
                .filter(io -> io.getCodItemOrcamentado() == codItemOrcamentado)
                .findFirst();

        if (itemToRemoveOpt.isPresent()) {
            ItemOrcamentado itemToRemove = itemToRemoveOpt.get();
            Item itemNoEstoque = buscarItemPorCodigo(itemToRemove.getItem().getCodItem()).orElse(null);

            if (itemNoEstoque != null) {
                itemNoEstoque.adicionarEstoque(itemToRemove.getQuantidadeUtilizada()); // Devolve ao estoque
            } else {
                System.err.println("Aviso: Item do catálogo (" + itemToRemove.getItem().getCodItem() + ") não encontrado ao remover ItemOrcamentado. Estoque não ajustado.");
            }
            return this.itensOrcamentados.removeIf(io -> io.getCodItemOrcamentado() == codItemOrcamentado);
        }
        System.err.println("Erro: Item orçado com código " + codItemOrcamentado + " não encontrado para remoção.");
        return false;
    }

    public void removerTodosItensOrcamentadosDoOrcamento(int codOrcamento) {
        List<ItemOrcamentado> itensParaRemover = this.itensOrcamentados.stream()
                .filter(io -> io.getOrcamento() != null && io.getOrcamento().getCodOrcamento() == codOrcamento)
                .collect(Collectors.toList());

        for (ItemOrcamentado itemOrcamentado : itensParaRemover) {
            Item itemNoEstoque = buscarItemPorCodigo(itemOrcamentado.getItem().getCodItem()).orElse(null);
            if (itemNoEstoque != null) {
                itemNoEstoque.adicionarEstoque(itemOrcamentado.getQuantidadeUtilizada()); // Devolve ao estoque
            } else {
                System.err.println("Aviso: Item do catálogo (" + itemOrcamentado.getItem().getCodItem() + ") não encontrado ao remover todos os ItemOrcamentado. Estoque não ajustado.");
            }
            // Não remove itemOrcamentado diretamente da lista original aqui para evitar ConcurrentModificationException
            // A remoção será feita após o loop ou com removeIf
        }
        this.itensOrcamentados.removeIf(io -> io.getOrcamento() != null && io.getOrcamento().getCodOrcamento() == codOrcamento);
    }

    public List<ItemOrcamentado> getItensOrcamentadosPorOrcamento(int codOrcamento) {
        return this.itensOrcamentados.stream()
                .filter(io -> io.getOrcamento() != null && io.getOrcamento().getCodOrcamento() == codOrcamento)
                .collect(Collectors.toList());
    }
    public List<ItemOrcamentado> getItemOrcamentado() { return new ArrayList<>(this.itensOrcamentados); }
    
    public void exportarOrcamentoParaTxt(Orcamento orcamento, List<ItemOrcamentado> itensOrcamentadosNoOrcamento, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("Extrato do Orçamento - Código: " + orcamento.getCodOrcamento());
            bw.newLine();
            bw.write("Data: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(orcamento.getData()));
            bw.newLine();
            bw.write("--------------------------------------------------");
            bw.newLine();
            bw.write("Itens Orçados:");
            bw.newLine();
            for (ItemOrcamentado itemOrcamentado : itensOrcamentadosNoOrcamento) {
                String itemName = (itemOrcamentado.getItem() != null) ? itemOrcamentado.getItem().getDescricao() : "N/A";
                double itemValue = (itemOrcamentado.getItem() != null) ? itemOrcamentado.getItem().getValor() : 0.0;

                bw.write(String.format("  - %s (Quant: %d) * %.2f(und) = %.2f",
                        itemName,
                        itemOrcamentado.getQuantidadeUtilizada(),
                        itemValue,
                        itemOrcamentado.getValorItemTotal()));
                bw.newLine();
            }
            bw.write("--------------------------------------------------");
            bw.newLine();
            bw.write(String.format("Valor Total (sem imposto): %.2f", orcamento.getValorTotal()));
            bw.newLine();
            bw.write(String.format("Valor Final com Imposto (6.5%%): %.2f", orcamento.getValorFinalComImposto()));
            bw.newLine();
            System.out.println("Orçamento exportado com sucesso para: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao exportar o orçamento para o arquivo: " + e.getMessage());
        }
    }
}