import javax.swing.SwingUtilities;
import view.*;
import controller.*;
import model.*;

public class Main {
	public static void main(String[] args) {
		OrcamentoController controller = new OrcamentoController();
		
        Categoria materiais = new Categoria(1, "Materiais");
        Categoria equipamentos = new Categoria(2, "Equipamentos");
        Categoria maoDeObra = new Categoria(3, "Mão de Obra");
        Categoria logistica = new Categoria(4, "Logística");

        controller.setCategoria(materiais);
        controller.setCategoria(equipamentos);
        controller.setCategoria(maoDeObra);
        controller.setCategoria(logistica);
        
        SwingUtilities.invokeLater(() -> {
            TelaPrincipal tela = new TelaPrincipal(controller);
            tela.setVisible(true);
        });
	}
}
