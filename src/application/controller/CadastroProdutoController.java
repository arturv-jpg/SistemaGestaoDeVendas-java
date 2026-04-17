package application.controller;

import java.text.DecimalFormat;
import java.util.List;

import application.model.ProdutoModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

public class CadastroProdutoController {
	
	@FXML private AnchorPane paneFundo;

	@FXML private TextField txtID;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtNome;
    @FXML private TextField txtCodBarras;
    @FXML private TextField txtPreco;

    @FXML private TableColumn<ProdutoModel, Integer> colID;
    @FXML private TableColumn<ProdutoModel, String> colNome;
    @FXML private TableColumn<ProdutoModel, String> colCodBarras;
    @FXML private TableColumn<ProdutoModel, String> colDescricao;
    @FXML private TableColumn<ProdutoModel, String> colCategoria;
    @FXML private TableColumn<ProdutoModel, Double> colPreco;
    @FXML private TableColumn<ProdutoModel, Integer> colQtd;

    @FXML private TableView<ProdutoModel> tabProdutos;

    private ObservableList<ProdutoModel> listaProdutos;

    DecimalFormat formatoReal = new DecimalFormat("#,##0.00");

    ProdutoModel produto = new ProdutoModel(0, null, null, null, null, 0, 0);

    // ================= SALVAR =================
    public void Salvar() {

        if(txtNome.getText().isEmpty() || txtCodBarras.getText().isEmpty() || 
           txtDescricao.getText().isEmpty() || txtCategoria.getText().isEmpty() || 
           txtPreco.getText().isEmpty()) {

            String erro="";
            if(txtNome.getText().isEmpty()) erro+="\nNome";
            if(txtCodBarras.getText().isEmpty()) erro+="\nCódigo de Barras";
            if(txtDescricao.getText().isEmpty()) erro+="\nDescrição";
            if(txtCategoria.getText().isEmpty()) erro+="\nCategoria";
            if(txtPreco.getText().isEmpty()) erro+="\nPreço";

            Alert mensagem = new Alert(Alert.AlertType.WARNING);
            mensagem.setContentText("Preencha os campos:" + erro);
            mensagem.showAndWait();
            return;
        }

        produto.setNome(txtNome.getText());
        produto.setCodBarras(txtCodBarras.getText());
        produto.setDescricao(txtDescricao.getText());
        produto.setCategoria(txtCategoria.getText());
        produto.setPreco(Double.parseDouble(txtPreco.getText().replace(",", ".")));

        // IMPORTANTE: só define 0 se for novo
        if(produto.getID() == 0){
            produto.setQuantidade(0);
        }

        produto.Salvar();

        Novo();
        ListarProdutosTab(null);
    }

    // ================= PESQUISAR =================
    public void Pesquisar() {
        if(!txtBuscar.getText().isEmpty()) {
            produto.Buscar(txtBuscar.getText());
            ListarProdutosTab(txtBuscar.getText());

            txtID.setText(String.format("%06d", produto.getID()));
            txtNome.setText(produto.getNome());
            txtCodBarras.setText(produto.getCodBarras());
            txtDescricao.setText(produto.getDescricao());
            txtCategoria.setText(produto.getCategoria());
            txtPreco.setText(formatoReal.format(produto.getPreco()));
            txtQuantidade.setText(String.valueOf(produto.getQuantidade()));
        } else {
            ListarProdutosTab(null);
        }
    }

    // ================= EXCLUIR =================
    public void Excluir() {

        if (produto.getID() <= 0) {
            Alert mensagem = new Alert(Alert.AlertType.WARNING);
            mensagem.setContentText("Nenhum produto selecionado!");
            mensagem.showAndWait();
            return;
        }

        produto.Excluir();

        Novo();
        ListarProdutosTab(null);
    }

    // ================= CLICK FORA =================
    @FXML
    public void clicarFundo(javafx.scene.input.MouseEvent event) {

        Object alvo = event.getTarget();

        if (!(alvo instanceof TableView) &&
            !(alvo instanceof TableRow) &&
            !(alvo instanceof TableCell)) {

            Novo();
        }
    }

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        colID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        colID.setCellFactory(c -> new TableCell<ProdutoModel, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                setText(empty ? null : String.format("%06d", id));
            }
        });

        colPreco.setCellFactory(c -> new TableCell<ProdutoModel, Double>() {
            @Override
            protected void updateItem(Double preco, boolean empty) {
                super.updateItem(preco, empty);
                setText(empty || preco == null ? null : formatoReal.format(preco));
            }
        });

        ListarProdutosTab(null);

        txtBuscar.setOnAction(e -> Pesquisar());

        tabProdutos.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {

                    produto = newSelection;

                    txtID.setText(String.format("%06d", produto.getID()));
                    txtNome.setText(produto.getNome());
                    txtCodBarras.setText(produto.getCodBarras());
                    txtDescricao.setText(produto.getDescricao());
                    txtCategoria.setText(produto.getCategoria());
                    txtQuantidade.setText(String.valueOf(produto.getQuantidade()));
                    txtPreco.setText(formatoReal.format(produto.getPreco()));
                }
            });
    }

    // ================= LISTAR =================
    public void ListarProdutosTab(String valor) {
        List<ProdutoModel> produtos = produto.ListarProdutos(valor);
        listaProdutos = FXCollections.observableArrayList(produtos);
        tabProdutos.setItems(listaProdutos);
    }

    // ================= NOVO =================
    public void Novo() {

        produto = new ProdutoModel(0, null, null, null, null, 0, 0);

        txtID.clear();
        txtNome.clear();
        txtCodBarras.clear();
        txtDescricao.clear();
        txtCategoria.clear();
        txtPreco.clear();
        txtQuantidade.clear();

        tabProdutos.getSelectionModel().clearSelection();
    }
}