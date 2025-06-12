package view;

import controller.OrcamentoController;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class TelaPrincipal extends JFrame {
    private OrcamentoController controller;

    public TelaPrincipal(OrcamentoController controller) {
        super("Calculadora de Orçamento de Obras");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        this.controller = controller;

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Gerenciamento de Itens", new CadastroItensPanel(this.controller));
        tabs.addTab("Orçamento", new OrcamentoPanel(this.controller));

        add(tabs);
        setVisible(true);
    }
}