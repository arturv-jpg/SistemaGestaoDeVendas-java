package application.model;

import java.sql.Connection;

import application.conexao;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProdutoModel {
	private int id;
	private String nome;
	private String codBarras;
	private String descricao;
	private String categoria;
	private double preco;
	private int quantidade;
	//CONSTRUTOR
	public ProdutoModel(
			int id, String nome,String codBarras, String descricao, String categoria, 
			double preco, int quantidade) {
		this.id=id;
		this.nome=nome;
		this.codBarras=codBarras;
		this.categoria=categoria;
		this.descricao = descricao;
		this.preco=preco;
		this.quantidade=quantidade;
	
	}
	//GETTERS
	public int getID() {return this.id;}
	public String getNome() {return this.nome;}
	public String getCodBarras() {return this.codBarras;}
	public String getDescricao() {return this.descricao;}
	public String getCategoria() {return this.categoria;}
	public double getPreco() {return this.preco;}
	public int getQuantidade() {return this.quantidade;}

	//SETTERS
	public void setID(int id) {this.id=id;}
	public void setNome(String nome) {this.nome=nome;}
	public void setCodBarras(String codBarras) {this.codBarras=codBarras;}
	public void setDescricao(String descricao) {this.descricao=descricao;}
	public void setCategoria(String categoria) {this.categoria=categoria;}
	public void setPreco(double preco) {this.preco=preco;}
	public void setQuantidade(int quantidade) {this.quantidade=quantidade;}

	public void Salvar() {
		try(Connection conn = conexao.getConnection();
				PreparedStatement consulta = conn.prepareStatement("insert into produto (nome,codBarras,descricao,categoria,preco,quantidade) values (?,?,?,?,?,?)");
				PreparedStatement consultaUpdate = 
				conn.prepareStatement("update produto set nome=?,codBarras=?,descricao=?,"+
				"categoria=?, preco=?,quantidade=? where id =?");){
			//VERIFICA SE EXISTE ID
			if(this.id>0) {// SE EXISTIR ALTERA SENÃO CADASTRA		
					consultaUpdate.setString(1,this.nome);
					consultaUpdate.setString(2,this.codBarras);
					consultaUpdate.setString(3,this.descricao);
					consultaUpdate.setString(4,this.categoria);
					consultaUpdate.setDouble(5,this.preco);
					consultaUpdate.setInt(6,this.quantidade);
					consultaUpdate.setInt(7,this.id);
					consultaUpdate.executeUpdate();
					//CRIA MENSAGEM
					Alert mensagem = new Alert(Alert.AlertType.INFORMATION);
					mensagem.setContentText("Produto Alterado!");
					mensagem.showAndWait();
			} else {
			consulta.setString(1,this.nome);
			consulta.setString(2,this.codBarras);
			consulta.setString(3,this.descricao);
			consulta.setString(4,this.categoria);
			consulta.setDouble(5,this.preco);
			consulta.setInt(6,this.quantidade);
			consulta.executeUpdate();
			
			//CRIA MENSAGEM
			Alert mensagem = new Alert(Alert.AlertType.CONFIRMATION);
			mensagem.setContentText("Produto Cadastrado!");
			mensagem.showAndWait();
			}
		}catch(Exception e) { e.printStackTrace();}
	}
	/*DESAFIO - INCLUIR CAMPO DE ID NO FOMRULARIO DE CADASTRO
	 * CRIAR CAMPO -> INFORMAR ID
	 * ENCAPSULAR COM @FXML
	 * ATRIBUIR INFORMAÇÕES NO CAMPO*/
	public void Buscar(String Valor) {
		try(Connection conn = conexao.getConnection();
			PreparedStatement consulta = conn.prepareStatement("select * from produto where descricao like ? or categoria like ? or nome like ?");){
			//COLOCA INFORMAÇÕES NOS PARAMETROS DA CONSULTA SQL REPRESENTADA POR ?
			consulta.setString(1,"%"+Valor+"%");
			consulta.setString(2,"%"+Valor+"%");
			consulta.setString(3,"%"+Valor+"%");
			//GUARDA O RESULTADO EM UMA VARIAVEL DO TIPO RESULTSET (TIPO DE DADOS SQL)
			ResultSet resultado= consulta.executeQuery();
			//VERIFICA SE RETORNOU DADOS NA CONSULTA
			if(resultado.next()) {
				this.id=resultado.getInt("id");
				this.nome=resultado.getString("nome");
				this.codBarras=resultado.getString("codBarras");
				this.descricao=resultado.getString("descricao");
				this.categoria=resultado.getString("categoria");
				this.quantidade=resultado.getInt("quantidade");
				this.preco=resultado.getDouble("preco");
				
			} else {
				//Mensagem de Produto não Encontrado
				Alert mensagem = new Alert(Alert.AlertType.ERROR);
				mensagem.setContentText("Produto não encontrado!");
				mensagem.showAndWait();
			}
		} catch(Exception e) { e.printStackTrace();}
	}

	public void Excluir() {
		try (Connection conn = conexao.getConnection();
			 PreparedStatement consulta = 
		conn.prepareStatement("delete from produto where id=?");){
			//VEREIFICA SE O PRODUTO TEM ID
			if (this.id>0) {
				consulta.setInt(1, this.id);
				consulta.executeUpdate();
				//EXIBE MENSAGEM DE EXCLUÍDO 
				Alert mensagem = new Alert(Alert.AlertType.CONFIRMATION);
				mensagem.setContentText("Produto Excluído!");
				mensagem.showAndWait();
			} else {
				//EXIBE MENSAGEM NÃO LOCALIZADO 
				Alert mensagem = new Alert(Alert.AlertType.CONFIRMATION);
				mensagem.setContentText("Produto Não Localizado!");
				mensagem.showAndWait();
			}
		}catch(Exception e) {e.printStackTrace();}
	}
	
	public List<ProdutoModel> ListarProdutos(String Valor) {
		List <ProdutoModel> produtos = new ArrayList<ProdutoModel>();
		try(Connection conn = conexao.getConnection();
			PreparedStatement consulta = conn.prepareStatement("select * from produto");
			PreparedStatement consultaWhere = conn.prepareStatement("select * from produto where nome like ?  or descricao like? or categoria like ?")){
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
	
	public void ProcessaEstoque(String operacao) {		
		MovimentacaoEstoqueModel movimentacao= new MovimentacaoEstoqueModel(
				0, this.id, this.nome, null, this.quantidade, operacao);
		if(this.id>0) {
			String sql = "update produto set quantidade = quantidade + ? where id=?";
			if (operacao.equals("Saida")) {
				sql = "update produto set quantidade = quantidade - ? where id=?";
			}
			
		try(Connection conn = conexao.getConnection();
				PreparedStatement consulta = conn.prepareStatement(sql);
			){
			consulta.setInt(1, this.quantidade);
			consulta.setInt(2, this.id);
			consulta.execute();
			movimentacao.InsereMovimentacao();
			
		}catch(Exception e) {e.printStackTrace();}
		}
	}
}
