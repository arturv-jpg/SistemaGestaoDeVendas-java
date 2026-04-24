package application.controller;

import java.text.DecimalFormat;
import java.util.List;

import application.model.ProdutoModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class CadastroProdutoController {
    
	@FXML private BorderPane paneFundo;

    @FXML private TextField txtID;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtBuscar;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtNome;
    @FXML private TextField txtCodBarras;
    @FXML private TextField txtPreco;
    
    // NOVOS CAMPOS
    @FXML private TextField txtPrecoCusto;
    @FXML private TextField txtMargemLucro;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private Button btnCalcularPreco;
    @FXML private Button btnBuscar;
    @FXML private Button btnSalvar;
    @FXML private Button btnExcluir;

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

            String erro = "";
            if(txtNome.getText().isEmpty()) erro += "\nNome";
            if(txtCodBarras.getText().isEmpty()) erro += "\nCódigo de Barras";
            if(txtDescricao.getText().isEmpty()) erro += "\nDescrição";
            if(txtCategoria.getText().isEmpty()) erro += "\nCategoria";
            if(txtPreco.getText().isEmpty()) erro += "\nPreço";  // ✅ CORRIGIDO (era "\Preço")

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
        
        // NOVOS CAMPOS
        if(!txtPrecoCusto.getText().isEmpty()) {
            produto.setPrecoCusto(Double.parseDouble(txtPrecoCusto.getText().replace(",", ".")));
        }
        if(!txtMargemLucro.getText().isEmpty()) {
            produto.setMargemLucro(Double.parseDouble(txtMargemLucro.getText().replace(",", ".")));
        }
        if(!txtEstoqueMinimo.getText().isEmpty()) {
            produto.setEstoqueMinimo(Integer.parseInt(txtEstoqueMinimo.getText()));
        }

        if(produto.getID() == 0){
            produto.setQuantidade(0);
        }

        produto.Salvar();
        Novo();
        ListarProdutosTab(null);
    }

    // ================= CALCULAR PREÇO =================
    @FXML
    public void calcularPreco() {
        try {
            if(txtPrecoCusto.getText().isEmpty() || txtMargemLucro.getText().isEmpty()) {
                alerta("Preencha o Preço de Custo e a Margem de Lucro (%);");
                return;
            }
            double custo = Double.parseDouble(txtPrecoCusto.getText().replace(",", "."));
            double margem = Double.parseDouble(txtMargemLucro.getText().replace(",", "."));
            double precoVenda = custo * (1 + margem / 100);
            txtPreco.setText(formatoReal.format(precoVenda));
        } catch(NumberFormatException e) {
            alerta("Valores inválidos para cálculo!");
        }
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
            // NOVOS
            txtPrecoCusto.setText(formatoReal.format(produto.getPrecoCusto()));
            txtMargemLucro.setText(String.valueOf(produto.getMargemLucro()));
            txtEstoqueMinimo.setText(String.valueOf(produto.getEstoqueMinimo()));
        } else {
            ListarProdutosTab(null);
        }
    }

    // ================= EXCLUIR =================
    @FXML
    public void Excluir() {

        ProdutoModel selecionado = tabProdutos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            alerta("Selecione um produto!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Deseja realmente excluir este produto?");

        confirm.showAndWait().ifPresent(resposta -> {

            if (resposta == ButtonType.OK) {

                try {

                    selecionado.Excluir();

                    alerta("Produto excluído com sucesso!");

                } catch (Exception e) {

                    if ("PRODUTO_COM_VENDA".equals(e.getMessage())) {

                        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                        a.setContentText("Produto já possui vendas.\nDeseja desativar?");

                        a.showAndWait().ifPresent(resp -> {
                            if (resp == ButtonType.OK) {
                                selecionado.desativar();
                                alerta("Produto desativado!");
                            }
                        });

                    } else {
                        alerta("Erro ao excluir: " + e.getMessage());
                    }
                }

                ListarProdutosTab(null);
                Novo();
            }
        });
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

        // 🔥 ALERTA DE ESTOQUE MÍNIMO: pinta a célula da quantidade em vermelho se <= estoque_minimo
        colQtd.setCellFactory(col -> new TableCell<ProdutoModel, Integer>() {
            @Override
            protected void updateItem(Integer qtd, boolean empty) {
                super.updateItem(qtd, empty);
                if (empty || qtd == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(qtd));
                    // Obtém o produto da linha atual
                    ProdutoModel produto = getTableView().getItems().get(getIndex());
                    if (produto != null && produto.getEstoqueMinimo() > 0 && qtd <= produto.getEstoqueMinimo()) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #cc0000;"); // vermelho claro
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        ListarProdutosTab(null);
        txtBuscar.setOnAction(e -> Pesquisar());
        
        if(btnCalcularPreco != null) {
            btnCalcularPreco.setOnAction(e -> calcularPreco());
        }

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
                    txtPrecoCusto.setText(formatoReal.format(produto.getPrecoCusto()));
                    txtMargemLucro.setText(String.valueOf(produto.getMargemLucro()));
                    txtEstoqueMinimo.setText(String.valueOf(produto.getEstoqueMinimo()));
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
        // NOVOS
        txtPrecoCusto.clear();
        txtMargemLucro.clear();
        txtEstoqueMinimo.clear();
        tabProdutos.getSelectionModel().clearSelection();
    }
    
    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}