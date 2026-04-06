package application.controller;

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
    
    @FXML private void initialize() {
    	itemProcessaEstoque.setOnAction(e->{AbrirProcessaEstoque();});
    }
    
    public void Sair() {
    	System.exit(0);
    }
    
    public void AbrirCadastroProduto() {
	try {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/view/CadastroProdutos.fxml"));
		Stage stage= new Stage();
		stage.setScene(new Scene (loader.load()));
		stage.show();
    } catch(Exception e) {
		e.printStackTrace();
	}
    }
    
    public void AbrirProcessaEstoque() {
	try {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/view/ProcessarEstoque.fxml"));
		Stage stage= new Stage();
		stage.setScene(new Scene (loader.load()));
		stage.show();
    } catch(Exception e) {
		e.printStackTrace();
	}
    }

    

}
