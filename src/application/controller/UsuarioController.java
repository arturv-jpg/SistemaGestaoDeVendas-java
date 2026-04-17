package application.controller;

import application.model.UsuarioModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UsuarioController {

    @FXML private TextField txtNome;
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    @FXML private ComboBox<String> cbTipo;

    private UsuarioModel usuario;

    @FXML
    public void initialize() {

        cbTipo.setItems(FXCollections.observableArrayList(
                "admin",
                "gerente",
                "vendedor",
                "estoquista"
        ));

        novo();
    }

    // SALVAR USUÁRIO
    @FXML
    public void salvar() {

        if(txtNome.getText().isEmpty() ||
           txtLogin.getText().isEmpty() ||
           txtSenha.getText().isEmpty() ||
           cbTipo.getValue() == null) {

            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setContentText("Preencha todos os campos!");
            a.show();
            return;
        }

        usuario = new UsuarioModel(
                0,
                txtNome.getText(),
                txtLogin.getText(),
                txtSenha.getText(),
                cbTipo.getValue()
        );

        usuario.salvar();

        Alert ok = new Alert(Alert.AlertType.INFORMATION);
        ok.setContentText("Usuário salvo com sucesso!");
        ok.show();

        novo();
    }

    // LIMPAR CAMPOS
    @FXML
    public void novo() {
        txtNome.clear();
        txtLogin.clear();
        txtSenha.clear();
        cbTipo.getSelectionModel().clearSelection();
    }
}