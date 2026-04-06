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
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;

public class ProcessaEstoque {

    @FXML private Button btnBuscar;
    @FXML private Button btnProcessar;
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
  
    //CRIANDO O OBJETO
    ProdutoModel produto= new ProdutoModel(0, null, null, null, null, 0, 0);
    
    @FXML
    public void initialize() {
    	colID.setCellValueFactory(new PropertyValueFactory<>("ID"));
    	colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
    	colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
    	colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
    	colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
    	colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
    	
    	ListarProdutosTab(null);
    	
    	//Seleciona item na tabela de produtos 
    	tabProdutos.getSelectionModel().selectedItemProperty().addListener(
		(obs, selecao , novaSelecao) ->{
			if (novaSelecao != null) {
				// Atualizar Model
				produto=novaSelecao;
				//Atualizar Campos
				txtID.setText(String.format("%06d", produto.getID()));
				txtNome.setText(produto.getNome());
				txtCodBarras.setText(produto.getCodBarras());
				txtQtd.setText("0");
				
			}
		});
    	
    	btnProcessar.setOnAction(e->{
    		produto.setQuantidade(Integer.parseInt(txtQtd.getText()));
    		//pega a informação selecionada se entrada ou saida
        	RadioButton operacao = (RadioButton) rdOperacao.getSelectedToggle();
    		produto.ProcessaEstoque(operacao.getText());
    		txtBuscar.clear();
    		txtID.clear();
    		txtCodBarras.clear();
    		txtNome.clear();
    		txtQtd.setText("0");
    		ListarProdutosTab(null);
    	});
    	btnBuscar.setOnAction(e->{Pesquisar();});
    }
  //METODO PARA BUSCAR O CADASTRO DO PRODUTO
    public void Pesquisar() {
    	//VERIFICA SE TEM TEXTO NO CAMPO DE BUSCA
    	//PARA PESQUISAR DE ACORDO COM O TEXTO DIGITADO
    	if(!txtBuscar.getText().isEmpty()) {    		
    		//executa o metodo de buscar
    		produto.Buscar(txtBuscar.getText());
    		ListarProdutosTab(txtBuscar.getText());
    		//informar os Valores nos campos do formulario
    		txtID.setText(String.format("%06d", produto.getID()));//Usa o Get Para buscar informação
    		txtNome.setText(produto.getNome()); 
    		txtCodBarras.setText(produto.getCodBarras());
    		txtQtd.setText("0");   		
    	} else { // SENÃO BUSCA TODOS OS PRODUTOS
    		//BUSCA TODOS OS PRODUTOS
    		ListarProdutosTab(null);
    	}
    }
    
    public List<ProdutoModel> ListarProdutos(String Valor) {
		List <ProdutoModel> produtos = new ArrayList<ProdutoModel>();
		try(Connection conn = conexao.getConnection();
			PreparedStatement consulta = conn.prepareStatement(
					"select * from produto");
			PreparedStatement consultaWhere = conn.prepareStatement(
					"select * from produto where nome like ?  or descricao like?"
					+ " or categoria like ?")){
			ResultSet resultado=null;
			if(Valor == null) {
				 resultado=consulta.executeQuery();
			} else {
				consultaWhere.setString(1, "%"+Valor+"%");
				consultaWhere.setString(2, "%"+Valor+"%");
				consultaWhere.setString(3, "%"+Valor+"%");
				resultado=consultaWhere.executeQuery();
			}					
			
			while (resultado.next()) {
				ProdutoModel p = new ProdutoModel(
						resultado.getInt("id"),
						resultado.getString("nome"),
						resultado.getString("codBarras"),
						resultado.getString("descricao"),
						resultado.getString("categoria"),
						resultado.getDouble("preco"),
						resultado.getInt("quantidade")				
						);
				produtos.add(p);
			}	
		}catch(Exception e) {e.printStackTrace();}
		return produtos;
	}
    
    public void ListarProdutosTab(String Valor) {
    	List <ProdutoModel> produtos = produto.ListarProdutos(Valor);
    	listaProdutos=FXCollections.observableArrayList(produtos);
    	tabProdutos.setItems(listaProdutos);
    	
    }
}
