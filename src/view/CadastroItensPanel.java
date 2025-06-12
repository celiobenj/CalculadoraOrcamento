package view;

import controller.OrcamentoController;
import model.Categoria;
import model.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class CadastroItensPanel extends JPanel {
    private OrcamentoController controller;

    // Componentes da UI
    private JTextField tfCodigo;
    private JTextField tfDesc;
    private JTextField tfValor;
    private JTextField tfQuant;
    private JComboBox<Categoria> cbCat;
    private JButton btnAdicionarAtualizar;
    private JButton btnRemover;
    private JButton btnLimparCampos;
    private JLabel lblStatus;

    private JTable tabelaItens;
    private DefaultTableModel tabelaItensModel;

    private Item itemSelecionadoParaEdicao; // Para controlar qual item está sendo editado

    public CadastroItensPanel(OrcamentoController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10)); // Adiciona espaçamento

        // --- Painel de Formulário e Ações (NORTH) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new TitledBorder("Cadastro e Edição de Itens"));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaçamento entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 0: Código
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1;
        tfCodigo = new JTextField(5);
        tfCodigo.setEditable(true); // Pode ser editado para novo item
        formPanel.add(tfCodigo, gbc);

        // Linha 1: Descrição
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3; // Ocupa mais colunas
        tfDesc = new JTextField(20);
        formPanel.add(tfDesc, gbc);
        gbc.gridwidth = 1; // Reseta para o padrão

        // Linha 2: Valor e Quantidade
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1;
        tfValor = new JTextField(7);
        formPanel.add(tfValor, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 3;
        tfQuant = new JTextField(5);
        formPanel.add(tfQuant, gbc);

        // Linha 3: Categoria
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        cbCat = new JComboBox<>();
        formPanel.add(cbCat, gbc);
        gbc.gridwidth = 1;

        // Linha 4: Botões de Ação
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnAdicionarAtualizar = new JButton("Adicionar Item");
        buttonPanel.add(btnAdicionarAtualizar);
        btnRemover = new JButton("Remover Item");
        btnRemover.setEnabled(false); // Desabilitado inicialmente
        buttonPanel.add(btnRemover);
        btnLimparCampos = new JButton("Limpar Campos");
        buttonPanel.add(btnLimparCampos);
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // --- Painel da Tabela de Itens (CENTER) ---
        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBorder(new TitledBorder("Itens Cadastrados"));

        String[] colunasItens = {"Cód.", "Descrição", "Valor Unit.", "Quantidade", "Categoria"};
        tabelaItensModel = new DefaultTableModel(colunasItens, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Apenas Descrição, Valor Unit. e Quantidade podem ser editados (colunas 1, 2, 3)
                // A categoria não é editável diretamente na tabela para evitar complexidade com JComboBox cell editor
                return column == 1 || column == 2 || column == 3;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Double.class; // Valor Unit.
                if (columnIndex == 3) return Integer.class; // Quantidade
                return String.class; // Outras colunas como String
            }
        };

        tabelaItens = new JTable(tabelaItensModel);
        tabelaItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Permite selecionar apenas uma linha
        tabelaItens.setFillsViewportHeight(true); // Ocupa a altura total do viewport

        // Configura renderizador para Valor (formato monetário)
        tabelaItens.getColumnModel().getColumn(2).setCellRenderer(new DecimalFormatRenderer());
        
        // Adiciona um CellEditorListener para pegar as edições na tabela
        tabelaItensModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();

                if (row == -1 || col == -1) return; // Mudança na estrutura, não em célula
                
                // Pega o item correspondente à linha editada
                int codItemEditado = (int) tabelaItensModel.getValueAt(row, 0); // Código na primeira coluna
                Optional<Item> optionalItem = controller.getItens().stream()
                                                    .filter(item -> item.getCodItem() == codItemEditado)
                                                    .findFirst();

                if (optionalItem.isPresent()) {
                    Item itemAjustado = optionalItem.get();
                    try {
                        switch (col) {
                            case 1: // Descrição
                                String novaDesc = tabelaItensModel.getValueAt(row, col).toString().trim();
                                if (novaDesc.isEmpty()) {
                                     JOptionPane.showMessageDialog(this, "A descrição não pode ser vazia.", "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                                     atualizarTabelaItens(); // Reverte a tabela
                                     return;
                                }
                                // Valida se a nova descrição já existe para outro item (se o código for diferente)
                                boolean descDuplicada = controller.getItens().stream()
                                                                .anyMatch(i -> i.getDescricao().equalsIgnoreCase(novaDesc) && i.getCodItem() != itemAjustado.getCodItem());
                                if (descDuplicada) {
                                    JOptionPane.showMessageDialog(this, "Já existe um item com esta descrição.", "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                                    atualizarTabelaItens(); // Reverte a tabela
                                    return;
                                }
                                itemAjustado.setDescricao(novaDesc);
                                break;
                            case 2: // Valor Unit.
                                double novoValor = Double.parseDouble(tabelaItensModel.getValueAt(row, col).toString().replaceAll(",", ".").trim());
                                if (novoValor <= 0) {
                                    JOptionPane.showMessageDialog(this, "O valor deve ser maior que zero.", "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                                    atualizarTabelaItens();
                                    return;
                                }
                                itemAjustado.setValor(novoValor);
                                break;
                            case 3: // Quantidade
                                int novaQuantidade = Integer.parseInt(tabelaItensModel.getValueAt(row, col).toString().trim());
                                if (novaQuantidade < 0) { // Permitir 0 para indicar falta de estoque
                                    JOptionPane.showMessageDialog(this, "A quantidade não pode ser negativa.", "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                                    atualizarTabelaItens();
                                    return;
                                }
                                itemAjustado.setQuantidade(novaQuantidade);
                                break;
                        }
                        controller.atualizarItem(itemAjustado); // Salva a alteração no controller
                        lblStatus.setText("Item '" + itemAjustado.getDescricao() + "' atualizado.");
                        atualizarTabelaItens(); // Atualiza a tabela para refletir a formatação ou reverter valores inválidos
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Valor inválido para o campo. Digite um número válido.", "Erro de Edição", JOptionPane.ERROR_MESSAGE);
                        atualizarTabelaItens(); // Reverte a tabela para o estado anterior
                    } catch (Exception ex) {
                         JOptionPane.showMessageDialog(this, "Erro ao atualizar item: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                         atualizarTabelaItens();
                    }
                }
            }
        });


        tablePanel.add(new JScrollPane(tabelaItens), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // --- Painel de Status (SOUTH) ---
        lblStatus = new JLabel(" ");
        lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblStatus, BorderLayout.SOUTH);

        // --- Inicialização e Listeners ---
        carregarCategorias();
        atualizarTabelaItens(); // Carrega os itens existentes na tabela

        // Listener para seleção de linha na tabela
        tabelaItens.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Evita múltiplos eventos durante a seleção
                int selectedRow = tabelaItens.getSelectedRow();
                if (selectedRow != -1) {
                    btnRemover.setEnabled(true);
                    btnAdicionarAtualizar.setText("Atualizar Item");
                    tfCodigo.setEditable(false); // Código não pode ser editado ao atualizar

                    // Preencher os campos com os dados do item selecionado
                    int cod = (int) tabelaItensModel.getValueAt(selectedRow, 0);
                    String desc = (String) tabelaItensModel.getValueAt(selectedRow, 1);
                    // O valor e quantidade podem vir como Double/Integer diretamente se a coluna foi configurada
                    Double val = (Double) tabelaItensModel.getValueAt(selectedRow, 2);
                    Integer quant = (Integer) tabelaItensModel.getValueAt(selectedRow, 3);
                    String catDesc = (String) tabelaItensModel.getValueAt(selectedRow, 4);

                    tfCodigo.setText(String.valueOf(cod));
                    tfDesc.setText(desc);
                    tfValor.setText(String.valueOf(val));
                    tfQuant.setText(String.valueOf(quant));

                    // Encontrar e selecionar a categoria correta no JComboBox
                    for (int i = 0; i < cbCat.getItemCount(); i++) {
                        if (cbCat.getItemAt(i).getDescricao().equals(catDesc)) {
                            cbCat.setSelectedIndex(i);
                            break;
                        }
                    }

                    // Encontra o objeto Item correspondente para edição
                    itemSelecionadoParaEdicao = controller.getItens().stream()
                                                    .filter(item -> item.getCodItem() == cod)
                                                    .findFirst().orElse(null);
                } else {
                    // Nenhuma linha selecionada, volta para o modo de adição
                    resetForm();
                }
            }
        });

        // Listener para o botão Adicionar/Atualizar
        btnAdicionarAtualizar.addActionListener(e -> adicionarOuAtualizarItem());

        // Listener para o botão Remover
        btnRemover.addActionListener(e -> removerItem());

        // Listener para o botão Limpar Campos
        btnLimparCampos.addActionListener(e -> resetForm());

        // Listener para atualizar a tabela e categorias quando o painel é mostrado
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                carregarCategorias();
                atualizarTabelaItens();
            }
        });
    }

    private void carregarCategorias() {
        cbCat.removeAllItems();
        List<Categoria> categorias = controller.getCategorias();
        if (categorias.isEmpty()) {
            cbCat.addItem(new Categoria(0, "Nenhuma Categoria Cadastrada"));
            cbCat.setEnabled(false);
            btnAdicionarAtualizar.setEnabled(false);
        } else {
            cbCat.setEnabled(true);
            btnAdicionarAtualizar.setEnabled(true);
            categorias.forEach(cbCat::addItem);
        }
    }

    private void atualizarTabelaItens() {
        tabelaItensModel.setRowCount(0); // Limpa a tabela
        List<Item> itens = controller.getItens(); // Pega a lista atualizada do controller
        
        if (!itens.isEmpty()) {
            for (Item item : itens) {
                tabelaItensModel.addRow(new Object[]{
                    item.getCodItem(),
                    item.getDescricao(),
                    item.getValor(), // O renderer cuidará da formatação
                    item.getQuantidade(),
                    item.getCategoria() != null ? item.getCategoria().getDescricao() : "N/A"
                });
            }
            tabelaItens.setEnabled(true);
        } else {
            // Se não há itens, desabilitar a tabela ou mostrar uma mensagem
            // tabelaItens.setEnabled(false); // Pode ser bom se quiser que não seja clicável
            lblStatus.setText("Nenhum item cadastrado.");
        }
        resetForm(); // Limpa os campos após a atualização
    }

    private void adicionarOuAtualizarItem() {
        int codigo;
        double valor;
        int quantidade;
        String descricao = tfDesc.getText().trim();
        Categoria categoriaSelecionada = (Categoria) cbCat.getSelectedItem();

        // Validações básicas
        if (descricao.isEmpty() || categoriaSelecionada == null || categoriaSelecionada.getCodCategoria() == 0) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos e selecione uma categoria válida.", "Erro de Cadastro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            codigo = Integer.parseInt(tfCodigo.getText().trim());
            valor = Double.parseDouble(tfValor.getText().trim());
            quantidade = Integer.parseInt(tfQuant.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Código, Valor e Quantidade devem ser números válidos.", "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (codigo <= 0 || valor <= 0 || quantidade < 0) { // Quantidade pode ser 0
            JOptionPane.showMessageDialog(this, "Código e Valor devem ser maiores que zero. Quantidade não pode ser negativa.", "Erro de Cadastro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (itemSelecionadoParaEdicao == null) { // Modo de Adição
            // Valida se o código ou descrição já existem para um novo item
            boolean codigoOuDescDuplicado = controller.getItens().stream()
                                                    .anyMatch(item -> item.getCodItem() == codigo || item.getDescricao().equalsIgnoreCase(descricao));
            if (codigoOuDescDuplicado) {
                JOptionPane.showMessageDialog(this, "Já existe um item com este código ou descrição.", "Erro de Cadastro", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Item novoItem = new Item(codigo, descricao, valor, quantidade, categoriaSelecionada);
            if (controller.setItem(novoItem)) { // Assumindo que setItem agora recebe um objeto Item e retorna boolean
                lblStatus.setText("Item '" + descricao + "' adicionado com sucesso!");
            } else {
                lblStatus.setText("Erro ao adicionar item."); // Mensagem mais genérica se o controller falhar por outro motivo
            }
        } else { // Modo de Atualização
            // Valida se a descrição (se alterada) não existe para outro item (com código diferente)
            boolean descDuplicadaParaOutroItem = controller.getItens().stream()
                                                        .anyMatch(item -> item.getDescricao().equalsIgnoreCase(descricao) && item.getCodItem() != itemSelecionadoParaEdicao.getCodItem());
            if (descDuplicadaParaOutroItem) {
                JOptionPane.showMessageDialog(this, "Já existe um outro item com esta descrição.", "Erro de Atualização", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            itemSelecionadoParaEdicao.setDescricao(descricao);
            itemSelecionadoParaEdicao.setValor(valor);
            itemSelecionadoParaEdicao.setQuantidade(quantidade);
            itemSelecionadoParaEdicao.setCategoria(categoriaSelecionada);

            if (controller.atualizarItem(itemSelecionadoParaEdicao)) { // Novo método no controller
                lblStatus.setText("Item '" + descricao + "' atualizado com sucesso!");
            } else {
                lblStatus.setText("Erro ao atualizar item.");
            }
        }
        atualizarTabelaItens(); // Atualiza a tabela após a operação
    }

    private void removerItem() {
        int selectedRow = tabelaItens.getSelectedRow();
        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja remover este item?", "Confirmar Remoção", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int codItem = (int) tabelaItensModel.getValueAt(selectedRow, 0);
                String descItem = (String) tabelaItensModel.getValueAt(selectedRow, 1);

                if (controller.removerItem(codItem)) { // Novo método no controller
                    lblStatus.setText("Item '" + descItem + "' removido.");
                } else {
                    lblStatus.setText("Erro ao remover item '" + descItem + "'.");
                }
                atualizarTabelaItens();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetForm() {
        tfCodigo.setText("");
        tfDesc.setText("");
        tfValor.setText("");
        tfQuant.setText("");
        cbCat.setSelectedIndex(0); // Volta para a primeira categoria
        btnAdicionarAtualizar.setText("Adicionar Item");
        btnRemover.setEnabled(false);
        tfCodigo.setEditable(true); // Permite editar o código para novo item
        tabelaItens.clearSelection(); // Limpa a seleção da tabela
        itemSelecionadoParaEdicao = null; // Reseta o item para edição
        lblStatus.setText(" "); // Limpa a mensagem de status
    }

    // --- Classe para formatar valores double em formato monetário na tabela
    
    class DecimalFormatRenderer extends DefaultTableCellRenderer {
        private static final java.text.DecimalFormat formatter = new java.text.DecimalFormat("R$ #,##0.00##");
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Formata apenas se o valor for um Double
            if (value instanceof Double) {
                value = formatter.format(value);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}