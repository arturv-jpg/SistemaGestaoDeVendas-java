package application.model;

import java.sql.Connection;
import java.sql.PreparedStatement;

import application.conexao;
import javafx.scene.control.Alert;

public class MovimentacaoEstoqueModel {
	private int id;
	private int idProd;
	private String nomeProd;
	private String data;
	private int quantidade;
	private String tipo;
	
	//CONSTRUTOR
	public MovimentacaoEstoqueModel(int id, int idProd, String nomeProd,
								String data, int quantidade, String tipo) {
		this.id=id;
		this.idProd=idProd;
		this.nomeProd=nomeProd;
		this.data=data;
		this.quantidade=quantidade;
		this.tipo=tipo;
	}
	//GETTERS
	public int getID() {return this.id;}
	public int getIdProd() {return this.idProd;}
	public String getNomeProd() {return this.nomeProd;}
	public String getData() {return this.data;}
	public int getQuantidade() {return this.quantidade;}
	public String getTipo() {return this.tipo;}
	//SETTERS
	public void setID(int id) {this.id=id;}
	public void setIdProd(int idProd) {this.idProd=idProd;}
	public void setNomeProd(String nomeProd) {this.nomeProd=nomeProd;}
	public void setData(String data) {this.data=data;}
	public void setQuantidade(int quantidade) {this.quantidade=quantidade;}
	public void tipo(String tipo) {this.tipo=tipo;}
	
	public void InsereMovimentacao() {
		try(Connection conn = conexao.getConnection();
			PreparedStatement consulta = conn.prepareStatement(
		"insert into movimentacaoEstoque (idProd,dataHora,quantidade,tipo)"+
		" values (?,NOW(),?,?)");){
			int tipo=0;
			if (this.tipo.equals("Saida")) { tipo=1;}
			
			consulta.setInt(1,this.idProd);
			consulta.setInt(2,this.quantidade);
			consulta.setInt(3,tipo);
			consulta.executeUpdate();
			
			//CRIA MENSAGEM
			Alert mensagem = new Alert(Alert.AlertType.CONFIRMATION);
			mensagem.setContentText("Estoque Processado!");
			mensagem.showAndWait();
		}catch(Exception e) {e.printStackTrace();}
	}
}
