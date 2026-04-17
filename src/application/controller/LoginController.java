package application.controller;

import application.model.UsuarioModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtSenha;
    
    @FXML
    public void entrar() {

        UsuarioModel usuario = UsuarioModel.autenticar(
            txtLogin.getText(),
            txtSenha.getText()
        );

        if(usuario != null) {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/application/view/Sistema.fxml")
                );

                Parent root = loader.load();

                // 👉 pega o controller do sistema
                SistemaController controller = loader.getController();

                // 👉 envia o usuário logado
                controller.setUsuarioLogado(usuario);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();

                // fecha login
                ((Stage) txtLogin.getScene().getWindow()).close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setContentText("Login ou senha inválidos!");
            erro.show();
        }
    }
}