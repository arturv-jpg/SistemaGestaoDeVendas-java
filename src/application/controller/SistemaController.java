package application.controller;

import application.model.UsuarioModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;

public class SistemaController {

	@FXML private Button btnUsuarios;
	@FXML private Button btnClientes;
	@FXML private Button btnProdutos;
	@FXML private Button btnEstoque;
	@FXML private Button btnVenda;
	@FXML private Button btnSair;

    @FXML
    private StackPane contentArea;

    private UsuarioModel usuarioLogado;

    @FXML
    private void initialize() {
       
    }

    public void setUsuarioLogado(UsuarioModel usuario) {
        this.usuarioLogado = usuario;
        configurarPermissoes();
    }

    private void configurarPermissoes() {
        if (usuarioLogado == null) return;

        String tipo = usuarioLogado.getTipo().toLowerCase();

        if (tipo.equals("gerente")) {
            esconder(btnUsuarios);
        }
        else if (tipo.equals("vendedor")) {
            esconder(btnUsuarios);
            esconder(btnProdutos);
            esconder(btnEstoque);
        }
        else if (tipo.equals("estoquista")) {
            esconder(btnUsuarios);
            esconder(btnClientes);
            esconder(btnVenda);
        }
    }

    // 🔥 NOVO MÉTODO (carrega dentro da tela)
    private void carregarTelaNoCentro(String caminhoFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFXML));
            Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Sair() {
        javafx.application.Platform.exit();
    }

    // 🔹 AGORA CARREGAM DENTRO DO SISTEMA

    public void AbrirCadastroProduto() {
        carregarTelaNoCentro("/application/view/CadastroProdutos.fxml");
    }

    public void AbrirCadastroCliente() {
        carregarTelaNoCentro("/application/view/CadastroClientes.fxml");
    }

    public void AbrirCadastroUsuario() {
        carregarTelaNoCentro("/application/view/CadastroUsuario.fxml");
    }

    // 🔹 ESSES PRECISAM PASSAR USUÁRIO → mantidos adaptados

    public void AbrirProcessaEstoque() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/view/ProcessarEstoque.fxml"));
            Parent root = loader.load();

            ProcessaEstoque controller = loader.getController();
            controller.setUsuarioLogado(usuarioLogado);
            controller.ListarProdutosTab(null);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AbrirVenda() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/view/Venda.fxml"));
            Parent root = loader.load();

            VendaController controller = loader.getController();
            controller.setUsuarioLogado(usuarioLogado);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void abrirDashboard() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(
            new javafx.scene.control.Label("Bem-vindo ao sistema!")
        );
    }
    private void esconder(Button btn) {
        btn.setVisible(false);
        btn.setManaged(false);
    }
}