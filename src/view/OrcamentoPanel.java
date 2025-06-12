package view;

import javax.swing.*;
import controller.OrcamentoController;
import model.Categoria;
import model.Item;
import model.ItemOrcamentado;
import model.Orcamento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator; // Para ordenar itens por categoria
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrcamentoPanel extends JPanel {
    private OrcamentoController controller;

    // --- Componentes da UI ---
    // Painel Superior (Controle do Orçamento)
    private JLabel lblOrcamentoAtual;
    private JButton btnNovoOrcamento;
    private JComboBox<Orcamento> cbOrcamentoExistente; // Para selecionar orçamentos anteriores

    // Painel Esquerdo (Seleção de Itens)
    private JList<Item> listaItensCatalogo;
    private DefaultListModel<Item> listaCatalogoModel;
    private JSpinner spQuantidadeItem; // Para a quantidade do item a adicionar
    private JButton btnAdicionarItemOrcamento;

    // Painel Direito (Itens Orçados no Orçamento Atual)
    private JTable tabelaItensOrcados;
    private DefaultTableModel tabelaOrcamentoModel;
    private JButton btnRemoverItemOrcado;
    private JButton btnEditarQuantidade; // Para editar quantidade de item orçado

    // Painel Inferior (Resumo e Cálculos)
    private JTextArea taResumoCategorias; // Exibir custos por categoria
    private JLabel lblValorTotalOrcamento;
    private JLabel lblValorFinalImposto;

    // Painel de Ações
    private JButton btnSalvarOrcamento;
    private JButton btnExportarOrcamento;

    // Variáveis de estado do orçamento
    private Orcamento orcamentoAtual;
    private List<ItemOrcamentado> itensOrcamentadosNoAtual; // Itens do orçamento atual

    // Formatação de números
    private DecimalFormat df = new DecimalFormat("R$ #,##0.00");

    public OrcamentoPanel(OrcamentoController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10)); // Espaçamento entre as principais áreas

        // --- 1. Painel de Controle do Orçamento (NORTH) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        topPanel.add(new JLabel("Orçamento Atual:"));
        lblOrcamentoAtual = new JLabel("Nenhum");
        topPanel.add(lblOrcamentoAtual);

        btnNovoOrcamento = new JButton("Novo Orçamento");
        topPanel.add(btnNovoOrcamento);

        topPanel.add(new JLabel("Ou Carregar:"));
        cbOrcamentoExistente = new JComboBox<>();
        cbOrcamentoExistente.setPreferredSize(new Dimension(150, 25));
        topPanel.add(cbOrcamentoExistente);
        
        add(topPanel, BorderLayout.NORTH);

        // --- 2 & 3. Painel Central (JSplitPane para seleção e itens orçados) ---
        JSplitPane centralSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centralSplitPane.setResizeWeight(0.5); // Divide o espaço igualmente

        // Painel Esquerdo: Seleção de Itens do Catálogo
        JPanel catalogoPanel = new JPanel(new BorderLayout(5, 5));
        catalogoPanel.setBorder(new TitledBorder("Catálogo de Itens Disponíveis"));

        listaCatalogoModel = new DefaultListModel<>();
        listaItensCatalogo = new JList<>(listaCatalogoModel);
        listaItensCatalogo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogoPanel.add(new JScrollPane(listaItensCatalogo), BorderLayout.CENTER);

        JPanel adicionarItemPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        adicionarItemPanel.add(new JLabel("Quantidade:"));
        spQuantidadeItem = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        adicionarItemPanel.add(spQuantidadeItem);
        btnAdicionarItemOrcamento = new JButton("Adicionar ao Orçamento");
        adicionarItemPanel.add(btnAdicionarItemOrcamento);
        catalogoPanel.add(adicionarItemPanel, BorderLayout.SOUTH);
        centralSplitPane.setLeftComponent(catalogoPanel);

        // Painel Direito: Itens no Orçamento Atual
        JPanel orcamentoItensPanel = new JPanel(new BorderLayout(5, 5));
        orcamentoItensPanel.setBorder(new TitledBorder("Itens no Orçamento Atual"));

        String[] colunasOrcamento = {"Cód.", "Descrição", "Valor Unit.", "Quantidade", "Subtotal"};
        tabelaOrcamentoModel = new DefaultTableModel(colunasOrcamento, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Apenas a coluna "Quantidade" pode ser editada (índice 3)
                return column == 3;
            }
        };
        
        tabelaItensOrcados = new JTable(tabelaOrcamentoModel);
        tabelaItensOrcados.setFillsViewportHeight(true);
        orcamentoItensPanel.add(new JScrollPane(tabelaItensOrcados), BorderLayout.CENTER);

        JPanel botoesOrcamentoItem = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnRemoverItemOrcado = new JButton("Remover Item");
        btnRemoverItemOrcado.setEnabled(false); // Inicia desabilitado
        botoesOrcamentoItem.add(btnRemoverItemOrcado);
        orcamentoItensPanel.add(botoesOrcamentoItem, BorderLayout.SOUTH);
        centralSplitPane.setRightComponent(orcamentoItensPanel);

        add(centralSplitPane, BorderLayout.CENTER);

        // --- 4. Painel Inferior (Resumo e Cálculos) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Resumo por Categoria
        taResumoCategorias = new JTextArea(5, 30); // 5 linhas, 30 colunas de largura
        taResumoCategorias.setEditable(false);
        taResumoCategorias.setBorder(new TitledBorder("Custos por Categoria"));
        bottomPanel.add(new JScrollPane(taResumoCategorias), BorderLayout.WEST);

        // Totais do Orçamento
        JPanel totaisPanel = new JPanel(new GridLayout(2, 1, 5, 5)); // 2 linhas, 1 coluna
        totaisPanel.setBorder(new TitledBorder("Totais do Orçamento"));
        lblValorTotalOrcamento = new JLabel("Valor Total (sem imposto): " + df.format(0));
        lblValorFinalImposto = new JLabel("Valor Final (com 6.5% imposto): " + df.format(0));
        lblValorTotalOrcamento.setFont(lblValorTotalOrcamento.getFont().deriveFont(Font.BOLD, 14f));
        lblValorFinalImposto.setFont(lblValorFinalImposto.getFont().deriveFont(Font.BOLD, 14f));
        totaisPanel.add(lblValorTotalOrcamento);
        totaisPanel.add(lblValorFinalImposto);
        bottomPanel.add(totaisPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- 5. Painel de Ações (SOUTH do bottomPanel ou à direita do centralSplitPane se preferir) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnSalvarOrcamento = new JButton("Salvar Orçamento");
        btnSalvarOrcamento.setEnabled(false); // Inicia desabilitado até ter itens
        actionPanel.add(btnSalvarOrcamento);
        btnExportarOrcamento = new JButton("Exportar Orçamento (.txt)");
        btnExportarOrcamento.setEnabled(false); // Inicia desabilitado
        actionPanel.add(btnExportarOrcamento);
        
        bottomPanel.add(actionPanel, BorderLayout.EAST); // Colocado à direita dos totais

        // --- Inicialização e Listeners ---
        iniciarNovoOrcamento(); // Inicia com um novo orçamento vazio
        carregarItensCatalogo(); // Carrega os itens disponíveis para seleção
        carregarOrcamentosExistentes(); // Carrega orçamentos anteriores no combobox

        // Listeners
        btnNovoOrcamento.addActionListener(e -> iniciarNovoOrcamento());
        btnAdicionarItemOrcamento.addActionListener(e -> adicionarItemAoOrcamento());
        btnRemoverItemOrcado.addActionListener(e -> removerItemDoOrcamento());
        btnSalvarOrcamento.addActionListener(e -> salvarOrcamento());
        btnExportarOrcamento.addActionListener(e -> exportarOrcamento());
        cbOrcamentoExistente.addActionListener(e -> carregarOrcamentoSelecionado());

        // Listener para habilita/desabilita botões de edição/remoção
        tabelaItensOrcados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Evita múltiplos eventos durante a seleção
                boolean itemSelected = tabelaItensOrcados.getSelectedRow() != -1;
                btnRemoverItemOrcado.setEnabled(itemSelected); // Habilita se for editar diretamente pela tabela
            }
        });
        
        tabelaItensOrcados.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 3) { // Se a coluna editada for a de Quantidade
                int row = e.getFirstRow();
                if (row >= 0 && row < itensOrcamentadosNoAtual.size()) { // Garante que a linha é válida
                    try {
                        int novaQuantidade = Integer.parseInt(tabelaOrcamentoModel.getValueAt(row, 3).toString());
                        ItemOrcamentado itemOrc = itensOrcamentadosNoAtual.get(row);
                        Item itemOriginal = itemOrc.getItem(); // O item do catálogo associado

                        if (novaQuantidade <= 0) {
                            JOptionPane.showMessageDialog(this, "Quantidade deve ser maior que zero.", "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                            // Reverte o valor na tabela para o anterior
                            tabelaOrcamentoModel.setValueAt(0, row, 3);
                            return; // Sai do listener
                        }
                        
                        // VERIFICAÇÃO DE ESTOQUE
                        if (itemOriginal != null && novaQuantidade > itemOriginal.getQuantidade()) {
                            JOptionPane.showMessageDialog(this,
                                    "Quantidade solicitada (" + novaQuantidade + ") excede o estoque disponível (" + itemOriginal.getQuantidade() + ") para " + itemOriginal.getDescricao() + ".",
                                    "Estoque Insuficiente",
                                    JOptionPane.WARNING_MESSAGE);
                            // Reverte o valor na tabela para o anterior
                            tabelaOrcamentoModel.setValueAt(itemOrc.getQuantidadeUtilizada(), row, 3);
                            return; // Sai do listener
                        }

                        // Se passou pelas validações:
                        itemOrc.setQuantidadeUtilizada(novaQuantidade);
                        itemOrc.setValorItemTotal(); // Recalcula o subtotal para o ItemOrcamentado
                        
                        // Atualiza a linha específica na tabela para refletir o novo subtotal formatado
                        // Não precisa limpar e recarregar a tabela inteira para uma única edição
                        tabelaOrcamentoModel.setValueAt(df.format(itemOrc.getValorItemTotal()), row, 4); // Atualiza a coluna "Subtotal"
                        
                        calcularETotalizarOrcamento(); // Recalcula os totais e resumo por categoria
                        
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Quantidade inválida. Digite um número inteiro.", "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                        // Reverte o valor na tabela
                        if (row >= 0 && row < itensOrcamentadosNoAtual.size()) {
                             tabelaOrcamentoModel.setValueAt(itensOrcamentadosNoAtual.get(row).getQuantidadeUtilizada(), row, 3);
                        }
                    }
                }
            }
        });

        
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                carregarItensCatalogo();
                carregarOrcamentosExistentes();
            }
        });
    }

    // --- Métodos de Lógica ---

    private void iniciarNovoOrcamento() {
        // Gera um novo código para o orçamento (simplificado, em um sistema real seria um ID sequencial ou UUID)
        int novoCod = controller.getOrcamento().isEmpty() ? 1 : controller.getOrcamento().stream().mapToInt(Orcamento::getCodOrcamento).max().getAsInt() + 1;
        orcamentoAtual = new Orcamento(novoCod);
        itensOrcamentadosNoAtual = new ArrayList<>(); // Lista vazia para o novo orçamento
        lblOrcamentoAtual.setText("ORC-" + orcamentoAtual.getCodOrcamento());
        
        atualizarTabelaOrcamento();
        calcularETotalizarOrcamento();
        btnSalvarOrcamento.setEnabled(false); // Inicia desabilitado
        btnExportarOrcamento.setEnabled(false); // Inicia desabilitado
        cbOrcamentoExistente.setSelectedItem(null); // Limpa seleção anterior no combobox
    }

    private void carregarOrcamentosExistentes() {
        cbOrcamentoExistente.removeAllItems();
        List<Orcamento> orcamentosExistentes = controller.getOrcamento();
        if (orcamentosExistentes.isEmpty()) {
            cbOrcamentoExistente.addItem(null); // Adiciona um item nulo para "Nenhum Orçamento"
            cbOrcamentoExistente.setEnabled(false);
        } else {
            cbOrcamentoExistente.setEnabled(true);
            orcamentosExistentes.forEach(cbOrcamentoExistente::addItem);
            cbOrcamentoExistente.setSelectedItem(null); // Inicia sem nada selecionado
        }
    }

    private void carregarOrcamentoSelecionado() {
        Orcamento selecionado = (Orcamento) cbOrcamentoExistente.getSelectedItem();
        if (selecionado != null) {
            orcamentoAtual = selecionado;
            // Carrega os itensOrcamentados para este orçamento específico
            itensOrcamentadosNoAtual = new ArrayList<>();
            for (ItemOrcamentado io : controller.getItemOrcamentado()) {
                if (io.getOrcamento().getCodOrcamento() == orcamentoAtual.getCodOrcamento()) {
                    itensOrcamentadosNoAtual.add(io);
                }
            }
            lblOrcamentoAtual.setText("ORC-" + orcamentoAtual.getCodOrcamento());
            atualizarTabelaOrcamento();
            calcularETotalizarOrcamento();
            btnSalvarOrcamento.setEnabled(true); // Habilita para salvar modificações
            btnExportarOrcamento.setEnabled(true);
        } else {
            // Se "Nenhum Orçamento" ou nada selecionado, iniciar um novo
            iniciarNovoOrcamento();
        }
    }

    void carregarItensCatalogo() {
        listaCatalogoModel.clear();
        List<Item> itens = controller.getItens();
        if (itens.isEmpty()) {
            listaCatalogoModel.addElement(new Item(0, "Nenhum item cadastrado.", 0, 0, null));
            listaItensCatalogo.setEnabled(false);
            btnAdicionarItemOrcamento.setEnabled(false);
        } else {
            listaItensCatalogo.setEnabled(true);
            btnAdicionarItemOrcamento.setEnabled(true);
            itens.forEach(listaCatalogoModel::addElement);
        }
    }

    private void adicionarItemAoOrcamento() {
        Item selectedItem = listaItensCatalogo.getSelectedValue();
        int quantidade = (int) spQuantidadeItem.getValue();

        if (selectedItem == null || selectedItem.getCodItem() == 0) { // Verifica o item "Nenhum item cadastrado"
            JOptionPane.showMessageDialog(this, "Selecione um item do catálogo para adicionar.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (quantidade <= 0) {
            JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- VERIFICAÇÃO DE ESTOQUE AO ADICIONAR UM NOVO ITEM ---
        if (quantidade > selectedItem.getQuantidade()) {
            JOptionPane.showMessageDialog(this,
                    "Não há estoque suficiente. Quantidade disponível para " + selectedItem.getDescricao() + ": " + selectedItem.getQuantidade(),
                    "Estoque Insuficiente",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean found = false;
        for (ItemOrcamentado io : itensOrcamentadosNoAtual) {
            if (io.getItem().getCodItem() == selectedItem.getCodItem()) {
                int novaQuantidadeTotal = io.getQuantidadeUtilizada() + quantidade;
                // --- VERIFICAÇÃO DE ESTOQUE AO ATUALIZAR UM ITEM EXISTENTE ---
                if (novaQuantidadeTotal > selectedItem.getQuantidade()) {
                    JOptionPane.showMessageDialog(this,
                            "A adição deste item fará com que a quantidade total (" + novaQuantidadeTotal + ") exceda o estoque disponível (" + selectedItem.getQuantidade() + ") para " + selectedItem.getDescricao() + ".",
                            "Estoque Insuficiente",
                            JOptionPane.WARNING_MESSAGE);
                    return; // Impede a adição e sai do método
                }
                io.setQuantidadeUtilizada(novaQuantidadeTotal);
                io.setValorItemTotal();
                found = true;
                break;
            }
        }
        if (!found) {
            // Gera um novo código para o ItemOrcamentado
            int novoCodItemOrcado = 1;
            if (!controller.getItemOrcamentado().isEmpty()) {
                novoCodItemOrcado = controller.getItemOrcamentado().stream().mapToInt(ItemOrcamentado::getCodItemOrcamentado).max().orElse(0) + 1;
            }
            ItemOrcamentado novoItemOrcado = new ItemOrcamentado(novoCodItemOrcado, orcamentoAtual, selectedItem, quantidade);
            itensOrcamentadosNoAtual.add(novoItemOrcado);
        }
        
        atualizarTabelaOrcamento();
        calcularETotalizarOrcamento();
        btnSalvarOrcamento.setEnabled(true); // Habilita o botão salvar
        btnExportarOrcamento.setEnabled(true); // Habilita o botão exportar
    }

    private void removerItemDoOrcamento() {
        int selectedRow = tabelaItensOrcados.getSelectedRow();
        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover este item do orçamento?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                itensOrcamentadosNoAtual.remove(selectedRow);
                atualizarTabelaOrcamento();
                calcularETotalizarOrcamento();
                if (itensOrcamentadosNoAtual.isEmpty()) {
                    btnSalvarOrcamento.setEnabled(false);
                    btnExportarOrcamento.setEnabled(false);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Método para atualizar a tabela de itens orçados
    private void atualizarTabelaOrcamento() {
        tabelaOrcamentoModel.setRowCount(0); // Limpa a tabela
        for (ItemOrcamentado itemOrcado : itensOrcamentadosNoAtual) {
            // Verifica se o item do ItemOrcamentado não é nulo antes de acessar seus atributos
            String itemDesc = (itemOrcado.getItem() != null) ? itemOrcado.getItem().getDescricao() : "Item Desconhecido";
            double itemValor = (itemOrcado.getItem() != null) ? itemOrcado.getItem().getValor() : 0.0;
            
            Object[] row = {
                itemOrcado.getItem().getCodItem(), // Supondo que itemOrcado.getItem() não é nulo aqui
                itemDesc,
                df.format(itemValor),
                itemOrcado.getQuantidadeUtilizada(),
                df.format(itemOrcado.getValorItemTotal())
            };
            tabelaOrcamentoModel.addRow(row);
        }
    }

    // Método para calcular totais e exibir resumo por categoria
    private void calcularETotalizarOrcamento() {
        double totalGeral = 0;
        Map<Categoria, Double> totalPorCategoria = new HashMap<>();

        for (ItemOrcamentado io : itensOrcamentadosNoAtual) {
            totalGeral += io.getValorItemTotal();
            if (io.getItem() != null && io.getItem().getCategoria() != null) {
                Categoria categoria = io.getItem().getCategoria();
                totalPorCategoria.put(categoria, totalPorCategoria.getOrDefault(categoria, 0.0) + io.getValorItemTotal());
            }
        }
        
        orcamentoAtual.setValorTotal(totalGeral); // Atualiza o valor total no objeto Orcamento

        lblValorTotalOrcamento.setText("Valor Total (sem imposto): " + df.format(orcamentoAtual.getValorTotal()));
        lblValorFinalImposto.setText("Valor Final (com 6.5% imposto): " + df.format(orcamentoAtual.getValorFinalComImposto()));

        // Atualiza resumo por categoria
        StringBuilder sb = new StringBuilder();
        sb.append("Resumo por Categoria:\n");
        // Ordena por nome de categoria
        totalPorCategoria.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(Categoria::getDescricao)))
            .forEach(entry -> sb.append(String.format("- %s: %s\n", entry.getKey().getDescricao(), df.format(entry.getValue()))));
        taResumoCategorias.setText(sb.toString());
    }

    private void salvarOrcamento() {
        if (orcamentoAtual == null || itensOrcamentadosNoAtual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há itens para salvar neste orçamento ou orçamento nulo.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lógica para verificar se é um novo orçamento ou uma atualização
        boolean isNewOrcamento = true;
        for (Orcamento existingOrc : controller.getOrcamento()) { // getOrcamento() retorna a lista em memória
            if (existingOrc.getCodOrcamento() == orcamentoAtual.getCodOrcamento()) {
                isNewOrcamento = false;
                break;
            }
        }

        // Antes de salvar/atualizar o orçamento, garantir que todos os ItemOrcamentado tenham o Orcamento certo
        for(ItemOrcamentado io : itensOrcamentadosNoAtual) {
            io.setOrcamento(orcamentoAtual); // Garante que a referência do orçamento está correta
            // Se o ItemOrcamentado não tiver um codItemOrcamentado (por exemplo, é um item novo na sessão)
            // você pode gerar um aqui ou no setItemOrcamentado do controller
            // Exemplo: if (io.getCodItemOrcamentado() == 0) io.setCodItemOrcamentado(gerarNovoCodItemOrcamentado());
        }

        if (isNewOrcamento) {
            // É um novo orçamento
            if (controller.setOrcamento(orcamentoAtual)) { // Tenta adicionar o novo orçamento
                // Adiciona todos os itens orçados a ele
                for (ItemOrcamentado io : itensOrcamentadosNoAtual) {
                    // Aqui, é importante que o 'io' tenha um codItemOrcamentado único.
                    // Se a geração for automática, deve ser feita antes de chamar setItemOrcamentado
                    // ou dentro de setItemOrcamentado se a lista for gerenciada por ele.
                    // Se o codItemOrcamentado for 0 ou duplicado, controller.setItemOrcamentado falhará.
                    controller.adicionarItemOrcamentado(io); // Adiciona cada item orçado
                }
                JOptionPane.showMessageDialog(this, "Orçamento " + orcamentoAtual.getCodOrcamento() + " salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao salvar novo orçamento (código duplicado ou inválido).", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // É uma atualização de um orçamento existente
            if (controller.atualizarOrcamento(orcamentoAtual)) { // Atualiza os dados do orçamento
                // Remove todos os itens orçados antigos associados a este orçamento
                controller.removerTodosItensOrcamentadosDoOrcamento(orcamentoAtual.getCodOrcamento());

                // Adiciona os itens orçados da tela como os novos itens deste orçamento
                for (ItemOrcamentado io : itensOrcamentadosNoAtual) {
                     // Novamente, garantir codItemOrcamentado único para cada 'io'
                    controller.adicionarItemOrcamentado(io);
                }
                JOptionPane.showMessageDialog(this, "Orçamento " + orcamentoAtual.getCodOrcamento() + " atualizado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar orçamento (código não encontrado ou inválido).", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Sempre recarregar a lista de orçamentos para refletir as mudanças
        carregarOrcamentosExistentes();
        // Você também pode querer limpar a seleção ou o formulário aqui após salvar
    }

    private void exportarOrcamento() {
        if (orcamentoAtual == null || itensOrcamentadosNoAtual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Não há orçamento para exportar.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }


        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new java.io.File("./data"));
        fileChooser.setDialogTitle("Salvar Orçamento como TXT");
        fileChooser.setSelectedFile(new java.io.File("Orcamento_" + orcamentoAtual.getCodOrcamento() + ".txt"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            // Garante que a extensão .txt seja adicionada se o usuário não o fizer
            if (!filePath.toLowerCase().endsWith(".txt")) {
                filePath += ".txt";
            }
            controller.exportarOrcamentoParaTxt(orcamentoAtual, itensOrcamentadosNoAtual, filePath);
            JOptionPane.showMessageDialog(this, "Orçamento exportado para " + filePath, "Exportação Concluída", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}