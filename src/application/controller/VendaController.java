package application.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.model.ClienteModel;
import application.model.ProdutoModel;
import application.model.VendaModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class VendaController implements Initializable {

    @FXML private TextField txtProduto, txtQtd;
    @FXML private TableView<ProdutoModel> tabItens;
    @FXML private TableColumn<ProdutoModel, String> colNome;
    @FXML private TableColumn<ProdutoModel, Integer> colQtd;
    @FXML private TableColumn<ProdutoModel, Double> colPreco;

    @FXML private ComboBox<ClienteModel> cbCliente;
    @FXML private Label lblTotal;

    private List<ProdutoModel> itens = new ArrayList<>();
    private double total = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        colNome.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNome()));
        colQtd.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getQuantidade()).asObject());
        colPreco.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getPreco()).asObject());

        carregarClientes();
    }

    private void carregarClientes() {
        List<ClienteModel> lista = ClienteModel.buscar("");
        cbCliente.setItems(FXCollections.observableArrayList(lista));

        cbCliente.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(ClienteModel c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty ? "" : c.getNome());
            }
        });

        cbCliente.setButtonCell(new ListCell<>() {
            protected void updateItem(ClienteModel c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty ? "" : c.getNome());
            }
        });
    }

    @FXML
    public void adicionarItem() {

        if(txtProduto.getText().isEmpty() || txtQtd.getText().isEmpty()) {
            alerta("Informe produto e quantidade!");
            return;
        }
        ProdutoModel p = new ProdutoModel();
        p.Buscar(txtProduto.getText());

        if(p.getID() == 0) {
            alerta("Produto não encontrado!");
            return;
        }

        int qtd;

        try {
            qtd = Integer.parseInt(txtQtd.getText());
        } catch(Exception e) {
            alerta("Quantidade inválida!");
            return;
        }
        //  RN02 - BLOQUEIO DE ESTOQUE
        if(p.getQuantidade() <= 0) {
            alerta("Produto sem estoque!");
            return;
        }
        if(qtd > p.getQuantidade()) {
            alerta("Quantidade maior que o estoque disponível!");
            return;
        }
        // 🔥 cria novo item (não altera o original)
        ProdutoModel item = new ProdutoModel(
            p.getID(),
            p.getNome(),
            p.getCodBarras(),
            p.getDescricao(),
            p.getCategoria(),
            p.getPreco(),
            qtd
        );
        itens.add(item);

        total += p.getPreco() * qtd;

        atualizarTabela();

        lblTotal.setText("Total: R$ " + total);

        txtProduto.clear();
        txtQtd.clear();
    }

    private void atualizarTabela() {
        tabItens.setItems(FXCollections.observableArrayList(itens));
    }

    @FXML
    public void finalizarVenda() {

        ClienteModel cliente = cbCliente.getValue();
        if(cliente == null) {
            alerta("Selecione um cliente!");
            return;
        }
        if(itens.isEmpty()) {
            alerta("Adicione itens à venda!");
            return;
        }
        VendaModel venda = new VendaModel(cliente.getId(), total);
        if(!venda.salvarVenda(itens)) {
            alerta("Erro: estoque insuficiente para finalizar!");
            return;
        }
        alerta("Venda finalizada!");
        itens.clear();
        total = 0;
        atualizarTabela();
        lblTotal.setText("Total: R$ 0.00");
    }
    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
    @FXML
    public void cancelarVenda() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Venda");
        dialog.setHeaderText("Digite o ID da venda:");
        dialog.setContentText("ID:");

        dialog.showAndWait().ifPresent(idStr -> {

            try {
                int idVenda = Integer.parseInt(idStr);

                TextInputDialog motivoDialog = new TextInputDialog();
                motivoDialog.setTitle("Motivo");
                motivoDialog.setHeaderText("Informe o motivo do cancelamento:");

                motivoDialog.showAndWait().ifPresent(motivo -> {

                    VendaModel v = new VendaModel(0, 0);
                    v.cancelarVenda(idVenda, motivo);

                });

            } catch (Exception e) {
                alerta("ID inválido!");
            }
        });
    }
}