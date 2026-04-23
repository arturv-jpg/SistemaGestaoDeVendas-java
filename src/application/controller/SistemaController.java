package application.controller;

import javafx.scene.control.Menu;
import application.model.UsuarioModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class SistemaController {

    @FXML private MenuItem itemClientes;
    @FXML private MenuItem itemProcessaEstoque;
    @FXML private MenuItem itemProdutos;
    @FXML private MenuItem itemSair;
    @FXML private MenuItem itemUsuarios;
    @FXML private MenuItem itemVenda;
    @FXML private Menu menuVendas;

    private UsuarioModel usuarioLogado;

    @FXML
    private void initialize() {
        // Configura as ações dos menus (como já existia)
        itemProdutos.setOnAction(e -> AbrirCadastroProduto());
        itemProcessaEstoque.setOnAction(e -> AbrirProcessaEstoque());
        itemClientes.setOnAction(e -> AbrirCadastroCliente());
        itemUsuarios.setOnAction(e -> AbrirCadastroUsuario());
        itemVenda.setOnAction(e -> AbrirVenda());
        itemSair.setOnAction(e -> Sair());
    }

    public void setUsuarioLogado(UsuarioModel usuario) {
        this.usuarioLogado = usuario;
        configurarPermissoes();
    }

    /**
     * Configura a visibilidade dos menus conforme o tipo de usuário.
     * Nenhuma funcionalidade antiga é removida – apenas escondemos menus.
     */
    private void configurarPermissoes() {
        if (usuarioLogado == null) return;

        String tipo = usuarioLogado.getTipo().toLowerCase();

        // Por padrão, tudo visível (admin). Vamos ocultando conforme o tipo.
        // ADMIN: vê tudo (já está visível)

        if (tipo.equals("gerente")) {
            // Gerente NÃO pode cadastrar usuários (opcional, mas recomendado)
            itemUsuarios.setVisible(false);
            // Gerente pode ver o resto: clientes, produtos, estoque, vendas.
        }
        else if (tipo.equals("vendedor")) {
            // Vendedor não pode gerenciar usuários, nem produtos, nem estoque
            itemUsuarios.setVisible(false);
            itemProdutos.setVisible(false);
            itemProcessaEstoque.setVisible(false);
            // Vendedor só vê clientes e vendas
        }
        else if (tipo.equals("estoquista")) {
            // Estoquista não vê usuários, clientes, vendas
            itemUsuarios.setVisible(false);
            itemClientes.setVisible(false);
            menuVendas.setVisible(false);
            // Estoquista vê produtos (para cadastrar/editar) e processar estoque
        }
        // Admin: mantém tudo visível (não precisa fazer nada)
    }

    // Métodos de abertura de tela (mantidos originais, sem alterações)
    private void abrirTela(String caminhoFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFXML));
            loader.setResources(null); // evita problemas com ResourceBundle
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Sair() {
        javafx.application.Platform.exit();
    }

    public void AbrirCadastroProduto() {
        abrirTela("/application/view/CadastroProdutos.fxml", "Cadastro de Produtos");
    }

    public void AbrirProcessaEstoque() {
        // Importante: passa o usuário logado para o controller
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/view/ProcessarEstoque.fxml"));
            loader.setResources(null);
            Parent root = loader.load();
            ProcessaEstoque controller = loader.getController();
            controller.setUsuarioLogado(usuarioLogado);
            Stage stage = new Stage();
            stage.setTitle("Processar Estoque");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void AbrirCadastroCliente() {
        abrirTela("/application/view/CadastroClientes.fxml", "Cadastro de Clientes");
    }

    public void AbrirCadastroUsuario() {
        abrirTela("/application/view/CadastroUsuario.fxml", "Cadastro de Usuários");
    }

    public void AbrirVenda() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/view/Venda.fxml"));
            loader.setResources(null);
            Parent root = loader.load();
            VendaController controller = loader.getController();
            controller.setUsuarioLogado(usuarioLogado);
            Stage stage = new Stage();
            stage.setTitle("Nova Venda");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}