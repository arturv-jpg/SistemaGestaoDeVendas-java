package application.controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import application.model.MovimentacaoEstoqueModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class HistoricoController implements Initializable {

    @FXML private TableView<MovimentacaoEstoqueModel> tabHistorico;

    @FXML private TableColumn<MovimentacaoEstoqueModel, Integer> colID;
    @FXML private TableColumn<MovimentacaoEstoqueModel, Integer> colIdProd;
    @FXML private TableColumn<MovimentacaoEstoqueModel, Integer> colQtd;

    @FXML private TableColumn<MovimentacaoEstoqueModel, String> colData;
    @FXML private TableColumn<MovimentacaoEstoqueModel, String> colNome;
    @FXML private TableColumn<MovimentacaoEstoqueModel, String> colTipo;

    @FXML private DatePicker dtInicio;
    @FXML private DatePicker dtFinal;

    @FXML private Label lblProd;

    private int idProduto;

    // 🔹 RECEBE O PRODUTO SELECIONADO
    public void setProduto(int id, String nome) {
        this.idProduto = id;
        lblProd.setText(nome);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        colID.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getID()).asObject());
        colIdProd.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getIdProd()).asObject());
        colQtd.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getQuantidade()).asObject());

        colData.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getData()));
        colNome.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNomeProd()));
        colTipo.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTipo()));
    }

    // 🔹 BOTÃO BUSCAR (FXML)
    @FXML
    public void buscar() {

        if(dtInicio.getValue() == null || dtFinal.getValue() == null) {
            alerta("Selecione as datas!");
            return;
        }

        buscarHistorico(idProduto, dtInicio.getValue(), dtFinal.getValue());
    }

    // 🔹 MÉTODO QUE ESTAVA FALTANDO (RESOLVE SEU ERRO)
    public void buscarHistorico(int idProd, LocalDate inicio, LocalDate fim) {

        MovimentacaoEstoqueModel m = new MovimentacaoEstoqueModel(0,0,null,null,0,null);

        List<MovimentacaoEstoqueModel> lista =
                m.HistoricoMovimentacao(idProd, inicio, fim);

        tabHistorico.setItems(FXCollections.observableArrayList(lista));
    }

    // 🔹 ALERTA PADRÃO
    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}