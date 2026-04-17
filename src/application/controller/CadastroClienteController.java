package application.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.model.ClienteModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class CadastroClienteController implements Initializable {

    @FXML private TextField txtID, txtNome, txtCPF, txtEmail, txtBuscar;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TableView<ClienteModel> tabClientes;
    @FXML private TableColumn<ClienteModel, Integer> colID;
    @FXML private TableColumn<ClienteModel, String> colNome, colCPF, colEmail, colStatus;
    @FXML private ListView<String> listaHistorico;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 🔥 Preenche ComboBox
        cbStatus.setItems(FXCollections.observableArrayList("Ativo", "Inativo"));
        cbStatus.setValue("Ativo"); // padrão

        colID.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId()).asObject());
        colNome.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNome()));
        colCPF.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCpf()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        tabClientes.setOnMouseClicked(e -> selecionar());

        atualizarTabela("");
    }

    @FXML
    public void Salvar() {

        String status = cbStatus.getValue() != null ? cbStatus.getValue() : "Ativo";

        int id = txtID.getText().isEmpty() ? 0 : Integer.parseInt(txtID.getText());

        ClienteModel c = new ClienteModel(
            id,
            txtNome.getText(),
            txtCPF.getText(),
            txtEmail.getText(),
            status
        );

        if(!ClienteModel.validarCPF(txtCPF.getText())) {
            alerta("CPF inválido!");
            return;
        }

        if(c.salvar()) {
            if(id > 0) {
                alerta("Cliente atualizado!");
            } else {
                alerta("Cliente cadastrado!");
            }

            atualizarTabela("");
            Novo();
        } else {
            alerta("CPF ou email já cadastrado!");
        }
    }
    @FXML
    public void Excluir() {

        if(txtID.getText().isEmpty()) {
            alerta("Selecione um cliente!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Deseja excluir este cliente?");
        confirm.showAndWait();

        if(confirm.getResult() == ButtonType.OK) {

            ClienteModel c = new ClienteModel(
                Integer.parseInt(txtID.getText()),
                null, null, null, null
            );

            if(c.excluir()) {
                alerta("Cliente excluído!");
                atualizarTabela("");
                Novo();
            } else {
                alerta("Erro ao excluir!");
            }
        }
    }

    @FXML
    public void Buscar() {
        atualizarTabela(txtBuscar.getText());
    }

    @FXML
    public void Novo() {
        txtID.clear();
        txtNome.clear();
        txtCPF.clear();
        txtEmail.clear();
        cbStatus.setValue("Ativo"); // 🔥 padrão ao limpar
        listaHistorico.getItems().clear();
    }

    private void atualizarTabela(String busca) {
        List<ClienteModel> lista = ClienteModel.buscar(busca);
        tabClientes.setItems(FXCollections.observableArrayList(lista));
    }

    private void selecionar() {
        ClienteModel c = tabClientes.getSelectionModel().getSelectedItem();

        if(c != null) {
            txtID.setText(String.valueOf(c.getId()));
            txtNome.setText(c.getNome());
            txtCPF.setText(c.getCpf());
            txtEmail.setText(c.getEmail());
            cbStatus.setValue(c.getStatus());

            // 🔥 HISTÓRICO
            List<String> hist = ClienteModel.historico(c.getId());
            listaHistorico.setItems(FXCollections.observableArrayList(hist));
        }
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}