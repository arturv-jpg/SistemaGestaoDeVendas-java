package application.controller;

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
    private UsuarioModel usuarioLogado;
    @FXML private MenuItem itemVenda;
    
    @FXML
    private void initialize() {

        itemProdutos.setOnAction(e -> AbrirCadastroProduto());
        itemProcessaEstoque.setOnAction(e -> AbrirProcessaEstoque());
        itemClientes.setOnAction(e -> AbrirCadastroCliente());
        itemSair.setOnAction(e -> Sair());
    }
    public void setUsuarioLogado(UsuarioModel usuario) {
        this.usuarioLogado = usuario;
        configurarPermissoes();
    }
    private void configurarPermissoes() {

        if(usuarioLogado == null) return;

        String tipo = usuarioLogado.getTipo();

        // Exemplo:
        if(tipo.equalsIgnoreCase("vendedor")) {
            // vendedor NÃO pode ver usuários
            itemUsuarios.setVisible(false);
        }

        if(tipo.equalsIgnoreCase("estoquista")) {
            itemUsuarios.setVisible(false);
            itemClientes.setVisible(false);
        }
    }
    // Método padrão para abrir telas
    private void abrirTela(String caminhoFXML, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFXML));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //  SAIR
    public void Sair() {
        javafx.application.Platform.exit();
    }
    //  PRODUTOS
    public void AbrirCadastroProduto() {
        abrirTela("/application/view/CadastroProdutos.fxml", "Cadastro de Produtos");
    }
    //  ESTOQUE
    public void AbrirProcessaEstoque() {
        abrirTela("/application/view/ProcessarEstoque.fxml", "Processar Estoque");
    }
    //  CLIENTES
    public void AbrirCadastroCliente() {
        abrirTela("/application/view/CadastroClientes.fxml", "Cadastro de Clientes");
    }
    //  USUARIOS
    public void AbrirCadastroUsuario() {
        abrirTela("/application/view/CadastroUsuario.fxml", "Cadastro de Usuários");
    }
    public void AbrirVenda() {
        abrirTela("/application/view/Venda.fxml", "Nova Venda");
    }
}
