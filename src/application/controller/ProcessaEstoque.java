package application.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import application.conexao;
import application.model.ProdutoModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ProcessaEstoque {

    @FXML private Button btnBuscar;
    @FXML private Button btnProcessar;
    @FXML private Button btnHistorico;

    @FXML private TextField txtBuscar;
    @FXML private TextField txtCodBarras;
    @FXML private TextField txtID;
    @FXML private TextField txtNome;
    @FXML private TextField txtQtd;

    @FXML private TableColumn<ProdutoModel, String> colCategoria;
    @FXML private TableColumn<ProdutoModel, String> colCodBarras;
    @FXML private TableColumn<ProdutoModel, String> colDescricao;
    @FXML private TableColumn<ProdutoModel, Integer> colID;
    @FXML private TableColumn<ProdutoModel, String> colNome;
    @FXML private TableColumn<ProdutoModel, Integer> colQuantidade;

    @FXML private TableView<ProdutoModel> tabProdutos;
    @FXML private ToggleGroup rdOperacao;

    private ObservableList<ProdutoModel> listaProdutos;

    ProdutoModel produto = new ProdutoModel(0, null, null, null, null, 0, 0);

    @FXML
    public void initialize() {

        colID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        ListarProdutosTab(null);

        // 🔥 começa desabilitado
        btnHistorico.setDisable(true);

        // 🔥 seleção da tabela
        tabProdutos.getSelectionModel().selectedItemProperty().addListener(
            (obs, selecao, novaSelecao) -> {

                if (novaSelecao != null) {

                    produto = novaSelecao;

                    txtID.setText(String.format("%06d", produto.getID()));
                    txtNome.setText(produto.getNome());
                    txtCodBarras.setText(produto.getCodBarras());
                    txtQtd.setText("0");

                    btnHistorico.setDisable(false); // habilita

                } else {
                    btnHistorico.setDisable(true); // desabilita
                }
            }
        );

        // 🔥 PROCESSAR
        btnProcessar.setOnAction(e -> {

            if (txtID.getText().isEmpty()) {
                alerta("Selecione um produto!");
                return;
            }
            if (txtQtd.getText().isEmpty()) {
                alerta("Informe a quantidade!");
                return;
            }
            try {
                int qtd = Integer.parseInt(txtQtd.getText());

                if (qtd <= 0) {
                    alerta("Quantidade deve ser maior que zero!");
                    return;
                }
                produto.setQuantidade(qtd);

                RadioButton operacao = (RadioButton) rdOperacao.getSelectedToggle();

                produto.ProcessaEstoque(operacao.getText());

                atualizarTabela();

            } catch (NumberFormatException ex) {
                alerta("Digite apenas números na quantidade!");
            }
        });

        btnBuscar.setOnAction(e -> Pesquisar());
        btnHistorico.setOnAction(e -> Historico());
    }

    // 🔥 ABRIR HISTÓRICO
    @FXML
    public void Historico() {

        if (txtID.getText().isEmpty()) return;

        try {

            int id = Integer.parseInt(txtID.getText());
            String nome = txtNome.getText();

            FXMLLoader loader = new FXMLLoader(
            	    getClass().getResource("/application/view/HistoricoProcessamento.fxml")
            	);
            
            Parent root = loader.load();

            HistoricoController controller = loader.getController();

            controller.setProduto(id, nome);

            controller.buscarHistorico(
                id,
                java.time.LocalDate.now().minusDays(30),
                java.time.LocalDate.now()
            );

            Stage stage = new Stage();
            stage.setTitle("Histórico");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔍 PESQUISAR
    public void Pesquisar() {

        if (!txtBuscar.getText().isEmpty()) {

            produto.Buscar(txtBuscar.getText());
            ListarProdutosTab(txtBuscar.getText());

            txtID.setText(String.format("%06d", produto.getID()));
            txtNome.setText(produto.getNome());
            txtCodBarras.setText(produto.getCodBarras());
            txtQtd.setText("0");

        } else {
            ListarProdutosTab(null);
        }
    }

    // 🔥 LISTAR PRODUTOS (BANCO)
    public List<ProdutoModel> ListarProdutos(String valor) {

        List<ProdutoModel> produtos = new ArrayList<>();

        try (Connection conn = conexao.getConnection();
             PreparedStatement consulta = conn.prepareStatement("SELECT * FROM produto");
             PreparedStatement consultaWhere = conn.prepareStatement(
                 "SELECT * FROM produto WHERE nome LIKE ? OR descricao LIKE ? OR categoria LIKE ?"
             )) {

            ResultSet resultado;

            if (valor == null) {
                resultado = consulta.executeQuery();
            } else {
                consultaWhere.setString(1, "%" + valor + "%");
                consultaWhere.setString(2, "%" + valor + "%");
                consultaWhere.setString(3, "%" + valor + "%");
                resultado = consultaWhere.executeQuery();
            }

            while (resultado.next()) {
                ProdutoModel p = new ProdutoModel(
                    resultado.getInt("id"),
                    resultado.getString("nome"),
                    resultado.getString("codigo_barras"),
                    resultado.getString("descricao"),
                    resultado.getString("categoria"),
                    resultado.getDouble("preco"),
                    resultado.getInt("quantidade")
                );
                produtos.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return produtos;
    }

    //  JOGAR NA TABELA
    public void ListarProdutosTab(String valor) {
        List<ProdutoModel> produtos = ListarProdutos(valor);
        listaProdutos = FXCollections.observableArrayList(produtos);
        tabProdutos.setItems(listaProdutos);
    }

    //  LIMPAR CAMPOS
    private void limparCampos() {
        txtBuscar.clear();
        txtID.clear();
        txtCodBarras.clear();
        txtNome.clear();
        txtQtd.setText("0");
        btnHistorico.setDisable(true);
    }
private void alerta(String msg) {
    javafx.scene.control.Alert a = new javafx.scene.control.Alert(
        javafx.scene.control.Alert.AlertType.WARNING
    );
    a.setContentText(msg);
    a.showAndWait();
}
private void atualizarTabela() {

    List<ProdutoModel> produtos = ListarProdutos(null);
    listaProdutos.setAll(produtos);

    txtQtd.setText("0");
}
}