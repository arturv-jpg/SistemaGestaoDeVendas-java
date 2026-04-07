package application.controller;

import java.time.LocalDate;
import java.util.List;

import application.model.MovimentacaoEstoqueModel;
import application.model.ProdutoModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class HistoricoController {

    @FXML private Button btnBuscar;
    @FXML private TableColumn<MovimentacaoEstoqueModel, String> colData;
    @FXML private TableColumn<MovimentacaoEstoqueModel, Integer> colID;
    @FXML private TableColumn<MovimentacaoEstoqueModel, Integer> colIdProd;
    @FXML private TableColumn<MovimentacaoEstoqueModel, String> colNome;
    @FXML private TableColumn<MovimentacaoEstoqueModel, Integer> colQtd;
    @FXML private TableColumn<MovimentacaoEstoqueModel, String> colTipo;
    @FXML private DatePicker dtFinal;
    @FXML private DatePicker dtInicio;
    @FXML private Label lblProd;
    @FXML private TableView<MovimentacaoEstoqueModel> tabHistorico;
    private ObservableList<MovimentacaoEstoqueModel> listaMovimentacao;
    private LocalDate hoje,primeiroDia, ultimoDia;
    MovimentacaoEstoqueModel Movimentacao = new MovimentacaoEstoqueModel(0, 0, null, null, 0, null);
    
    @FXML
    public void initialize() {
    	//ATRIBUI O TIPO DE INFORMAÇÃO DOS GETTERS DA MODEL EX.: return this.id;
    	colID.setCellValueFactory(new PropertyValueFactory<>("ID"));
    	colIdProd.setCellValueFactory(new PropertyValueFactory<>("idProd"));
    	colData.setCellValueFactory(new PropertyValueFactory<>("data"));
    	colNome.setCellValueFactory(new PropertyValueFactory<>("nomeProd"));
    	colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
    	colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
    	//Data atual
    	hoje = LocalDate.now();
    	//Primeiro dia do Mês
    	primeiroDia=hoje.withDayOfMonth(1);
    	//Ultimo dia do Mês
    	ultimoDia = hoje.withDayOfMonth(hoje.lengthOfMonth());  
    	
    	dtInicio.setValue(primeiroDia);
    	dtFinal.setValue(ultimoDia);
    	
    	btnBuscar.setOnAction(e-> buscarHistorico(Movimentacao.getIdProd(), 
    					dtInicio.getValue(), 
    					dtFinal.getValue())
    	);

    }
    
    public void buscarHistorico(int idProd, LocalDate dataInicio, LocalDate dataFinal) {
    	List <MovimentacaoEstoqueModel> listaHitorico = 
    			Movimentacao.HistoricoMovimentacao(idProd, dataInicio, dataFinal);
    	listaMovimentacao= FXCollections.observableArrayList(listaHitorico);
    	tabHistorico.setItems(listaMovimentacao);
    	lblProd.setText(Movimentacao.getNomeProd());
    	dtInicio.setValue(dataInicio);
    	dtFinal.setValue(dataFinal);
    	
    }
}

