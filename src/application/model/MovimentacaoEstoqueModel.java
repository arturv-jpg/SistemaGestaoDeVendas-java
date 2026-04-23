package application.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import application.conexao;
import javafx.scene.control.Alert;

public class MovimentacaoEstoqueModel {
    private int id;
    private int idProd;
    private String nomeProd;
    private String data;
    private int quantidade;
    private String tipo;
    private int idUsuario; // NOVO

    // Construtor atualizado
    public MovimentacaoEstoqueModel(int id, int idProd, String nomeProd,
                                    String data, int quantidade, String tipo, int idUsuario) {
        this.id = id;
        this.idProd = idProd;
        this.nomeProd = nomeProd;
        this.data = data;
        this.quantidade = quantidade;
        this.tipo = tipo;
        this.idUsuario = idUsuario;
    }

    // GETTERS (adicione o getter)
    public int getID() { return id; }
    public int getIdProd() { return idProd; }
    public String getNomeProd() { return nomeProd; }
    public String getData() { return data; }
    public int getQuantidade() { return quantidade; }
    public String getTipo() { return tipo; }
    public int getIdUsuario() { return idUsuario; }

    // SETTERS
    public void setID(int id) { this.id = id; }
    public void setIdProd(int idProd) { this.idProd = idProd; }
    public void setNomeProd(String nomeProd) { this.nomeProd = nomeProd; }
    public void setData(String data) { this.data = data; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    // Método para inserir movimentação com idUsuario
    public void InsereMovimentacao(int idUsuario) {
        try (Connection conn = conexao.getConnection();
             PreparedStatement consulta = conn.prepareStatement(
                 "INSERT INTO movimentacaoEstoque (idProduto, dataHora, quantidade, tipo, idUsuario) VALUES (?, NOW(), ?, ?, ?)")) {
            int tipoInt = this.tipo.equals("Saida") ? 1 : 0;
            consulta.setInt(1, this.idProd);
            consulta.setInt(2, this.quantidade);
            consulta.setInt(3, tipoInt);
            consulta.setInt(4, idUsuario);
            consulta.executeUpdate();
            Alert mensagem = new Alert(Alert.AlertType.CONFIRMATION);
            mensagem.setContentText("Estoque Processado!");
            mensagem.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para buscar histórico (ajuste na consulta para retornar idUsuario se quiser)
    public List<MovimentacaoEstoqueModel> HistoricoMovimentacao(int idProd, LocalDate dataInicio, LocalDate dataFim) {
        List<MovimentacaoEstoqueModel> movimentacao = new ArrayList<>();
        try (Connection conn = conexao.getConnection()) {
            String sql = "SELECT DATE_FORMAT(m.dataHora,'%d/%m/%y') as data,"
                    + "m.id, m.idProduto, p.nome, m.quantidade,"
                    + "(CASE WHEN m.tipo=0 THEN 'Entrada' WHEN m.tipo=1 THEN 'Saida' END) as tipo,"
                    + "m.idUsuario "
                    + "FROM produto p INNER JOIN movimentacaoEstoque m ON p.id = m.idProduto "
                    + "WHERE m.idProduto=? AND DATE(m.dataHora) BETWEEN ? AND ?";
            PreparedStatement consulta = conn.prepareStatement(sql);
            consulta.setInt(1, idProd);
            consulta.setDate(2, java.sql.Date.valueOf(dataInicio));
            consulta.setDate(3, java.sql.Date.valueOf(dataFim));
            ResultSet resultado = consulta.executeQuery();
            while (resultado.next()) {
                MovimentacaoEstoqueModel m = new MovimentacaoEstoqueModel(
                    resultado.getInt("id"),
                    resultado.getInt("idProduto"),
                    resultado.getString("nome"),
                    resultado.getString("data"),
                    resultado.getInt("quantidade"),
                    resultado.getString("tipo"),
                    resultado.getInt("idUsuario")
                );
                movimentacao.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movimentacao;
    }
}