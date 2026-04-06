package application.controller;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import application.model.ProdutoModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CadastroProdutoController {
	
	@FXML private TextField txtID;
    @FXML private TextField txtQuantidade;
    @FXML private Button btnSalvar;
    @FXML private TextField txtBuscar;
    @FXML private Button btnBuscar;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtDescricao;
    @FXML private TextField txtNome;
    @FXML private TextField txtCodBarras;
    @FXML private TextField txtPreco;
    @FXML private TableColumn<ProdutoModel, String> colCategoria;
    @FXML private TableColumn<ProdutoModel, String> colDescricao;
    @FXML private TableColumn<ProdutoModel, Integer> colID;
    @FXML private TableColumn<ProdutoModel, String> colNome;
    @FXML private TableColumn<ProdutoModel, String> colCodBarras;
    @FXML private TableColumn<ProdutoModel, Double> colPreco;
    @FXML private TableColumn<ProdutoModel, String> colQtd;
    @FXML private TableView<ProdutoModel> tabProdutos;
    private ObservableList<ProdutoModel> listaProdutos;
    DecimalFormat formatoReal = new DecimalFormat("#,##0.00");
    //NumberFormat formatoReal = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
    
    
    //CRIANDO O OBJETO
    ProdutoModel produto= new ProdutoModel(0, null, null, null, null, 0, 0);
    //METODO PARA SALVAR O CADASTRO DO PRODUTO
    public void Salvar() {
    	//VERIFICA SE TEM ALGUM CAMPO VAZIO
    	if(txtNome.getText().isEmpty() || txtCodBarras.getText().isEmpty() || txtDescricao.getText().isEmpty() || 
    		txtCategoria.getText().isEmpty() || txtPreco.getText().isEmpty() ) {
    		//ARMAZENA CAMPO QUE ESTÁ VAZIO
    		String erro="";
    		if(txtNome.getText().isEmpty()) {erro=erro+"\nCampo Nome em Branco!";}
    		if(txtDescricao.getText().isEmpty()) {erro=erro+"\nCampo Descricao em Branco!";}
    		if(txtCategoria.getText().isEmpty()) {erro=erro+"\nCampo Categoria em Branco!";}
    		if(txtPreco.getText().isEmpty()) {erro=erro+"\nCampo Preco em Branco!";}
    		//EXIBE UMA MENSAGEM PARA PREENCHER OS CAMPOS
    		Alert mensagem = new Alert(Alert.AlertType.INFORMATION);
			mensagem.setContentText("Preenchaos campos:"+erro);
			mensagem.showAndWait();
    	} else {
    		//INCLUIR AS INFORMAÇÕES NO OBJETO PRODUTO
        	produto.setNome(txtNome.getText());
        	produto.setCodBarras(txtCodBarras.getText());
        	produto.setDescricao(txtDescricao.getText());
        	produto.setCategoria(txtCategoria.getText());
        	produto.setPreco(Double.parseDouble(txtPreco.getText().replace(",", ".")));
        	produto.setQuantidade(0);
        	
    		//SALVA PRODUTOS NO BANCO DE DADOS
    		produto.Salvar();

			//LIMPA OS CAMPOS
        	txtNome.setText("");
        	txtCodBarras.setText("");
        	txtDescricao.setText("");
        	txtCategoria.setText("");
        	txtPreco.setText("");
        	txtQuantidade.setText("");
    	}    	
    	ListarProdutosTab(null);

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
    		txtDescricao.setText(produto.getDescricao());
    		txtCategoria.setText(produto.getCategoria());
    		txtPreco.setText(formatoReal.format(produto.getPreco()));
    		txtQuantidade.setText(String.valueOf(produto.getQuantidade()));   		
    	} else { // SENÃO BUSCA TODOS OS PRODUTOS
    		//BUSCA TODOS OS PRODUTOS
    		ListarProdutosTab(null);
    	}
    }
    //METODO PARA EXCLUIR O CADASTRO
    public void Excluir() {
    	produto.Excluir();
    	txtNome.setText("");
		txtDescricao.setText("");
		txtCategoria.setText("");
		txtPreco.setText("");
		txtQuantidade.clear();
		ListarProdutosTab(null);
    }
    //O METODO INITIALIZE VINCULA DIRETAMENTE O CONTROLE COM O FXML
    //A PALAVRA FXML É RESERVADA DO JAVAFX PARA REALIZAR A INTERAÇÃO
    @FXML
    public void initialize() {
    	//ATRIBUI O TIPO DE INFORMAÇÃO DOS GETTERS DA MODEL EX.: return this.id;
    	colID.setCellValueFactory(new PropertyValueFactory<>("ID"));
    	colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
    	colCodBarras.setCellValueFactory(new PropertyValueFactory<>("codBarras"));
    	colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
    	colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
    	colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
    	colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
    	
    	//colocar formato de "000000" na tabela de produtos
    	colID.setCellFactory(c -> new TableCell<ProdutoModel, Integer>() {
    	    @Override
    	    protected void updateItem(Integer id, boolean empty) {
    	        super.updateItem(id, empty);
    	        setText(empty ? null : String.format("%06d", id));
    	    }
    	});
    	//colocar formato de 0,00
    	colPreco.setCellFactory(c -> new TableCell<ProdutoModel, Double>() {
    	    @Override
    	    protected void updateItem(Double preco, boolean empty) {
    	        super.updateItem(preco, empty);
    	        setText(empty || preco == null ? null : new DecimalFormat("#,##0.00").format(preco));
    	    }
    	});
    	
    	ListarProdutosTab(null);
    	//Tecla enter no campo de pesquisa
    	txtBuscar.setOnAction(e->{btnBuscar.fire();}); // executa o click do butão buscar
    	
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
				txtDescricao.setText(produto.getDescricao());
				txtCategoria.setText(produto.getCategoria());
				txtQuantidade.setText(String.valueOf(produto.getQuantidade()));
				txtPreco.setText(formatoReal.format(produto.getPreco()));
				
			}
		});
    	
    }
    
    public void ListarProdutosTab(String Valor) {
    	List <ProdutoModel> produtos = produto.ListarProdutos(Valor);
    	listaProdutos=FXCollections.observableArrayList(produtos);
    	tabProdutos.setItems(listaProdutos);
    	
    }
}
