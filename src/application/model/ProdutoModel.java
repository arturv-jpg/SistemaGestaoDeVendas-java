package application.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import application.conexao;
import javafx.scene.control.Alert;

public class ProdutoModel {

    private int id;
    private String nome;
    private String codBarras;
    private String descricao;
    private String categoria;
    private double preco;
    private int quantidade;

    public ProdutoModel() {}

    public ProdutoModel(int id, String nome, String codBarras, String descricao,
                        String categoria, double preco, int quantidade) {
        this.id = id;
        this.nome = nome;
        this.codBarras = codBarras;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    // GETTERS
    public int getID() { return id; }
    public String getNome() { return nome; }
    public String getCodBarras() { return codBarras; }
    public String getDescricao() { return descricao; }
    public String getCategoria() { return categoria; }
    public double getPreco() { return preco; }
    public int getQuantidade() { return quantidade; }

    // SETTERS
    public void setID(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setCodBarras(String codBarras) { this.codBarras = codBarras; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setPreco(double preco) { this.preco = preco; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    // SALVAR
    public void Salvar() {
        try(Connection conn = conexao.getConnection()) {

            if(this.id > 0) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE produto SET nome=?, codigo_barras=?, descricao=?, categoria=?, preco=?, quantidade=? WHERE id=?"
                );

                ps.setString(1, nome);
                ps.setString(2, codBarras);
                ps.setString(3, descricao);
                ps.setString(4, categoria);
                ps.setDouble(5, preco);
                ps.setInt(6, quantidade);
                ps.setInt(7, id);
                ps.executeUpdate();

                alerta("Produto Alterado!");

            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO produto (nome, codigo_barras, descricao, categoria, preco, quantidade) VALUES (?,?,?,?,?,?)"
                );

                ps.setString(1, nome);
                ps.setString(2, codBarras);
                ps.setString(3, descricao);
                ps.setString(4, categoria);
                ps.setDouble(5, preco);
                ps.setInt(6, quantidade);
                ps.executeUpdate();

                alerta("Produto Cadastrado!");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // LISTAR
    public List<ProdutoModel> ListarProdutos(String valor) {
        List<ProdutoModel> lista = new ArrayList<>();

        try(Connection conn = conexao.getConnection()) {

            PreparedStatement ps;

            if(valor == null || valor.isEmpty()) {
                ps = conn.prepareStatement("SELECT * FROM produto");
            } else {
                ps = conn.prepareStatement(
                    "SELECT * FROM produto WHERE nome LIKE ? OR descricao LIKE ? OR categoria LIKE ?"
                );

                String busca = "%" + valor + "%";
                ps.setString(1, busca);
                ps.setString(2, busca);
                ps.setString(3, busca);
            }

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                lista.add(new ProdutoModel(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("codigo_barras"),
                    rs.getString("descricao"),
                    rs.getString("categoria"),
                    rs.getDouble("preco"),
                    rs.getInt("quantidade")
                ));
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // EXCLUIR
    public void Excluir() {
        try(Connection conn = conexao.getConnection()) {

            if(id > 0) {

                PreparedStatement delMov = conn.prepareStatement(
                    "DELETE FROM movimentacaoEstoque WHERE idProduto=?");
                delMov.setInt(1, id);
                delMov.executeUpdate();

                PreparedStatement delProd = conn.prepareStatement(
                    "DELETE FROM produto WHERE id=?");
                delProd.setInt(1, id);
                delProd.executeUpdate();

                alerta("Produto Excluído!");

            } else {
                alerta("Produto não selecionado!");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // PROCESSAR ESTOQUE
    public void ProcessaEstoque(String operacao) {

        if (id <= 0) return;

        try(Connection conn = conexao.getConnection()) {

            // 🔍 verifica estoque atual
            PreparedStatement check = conn.prepareStatement(
                "SELECT quantidade FROM produto WHERE id=?"
            );
            check.setInt(1, id);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {

                int estoqueAtual = rs.getInt("quantidade");

                // 🚫 BLOQUEIO
                if (operacao.equals("Saida") && quantidade > estoqueAtual) {
                    alerta("Estoque insuficiente!");
                    return;
                }

                String sql = "UPDATE produto SET quantidade = quantidade + ? WHERE id=?";

                if (operacao.equals("Saida")) {
                    sql = "UPDATE produto SET quantidade = quantidade - ? WHERE id=?";
                }

                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, quantidade);
                ps.setInt(2, id);
                ps.execute();

                // 🔥 registra movimentação
                MovimentacaoEstoqueModel mov = new MovimentacaoEstoqueModel(
                    0, id, nome, null, quantidade, operacao
                );
                mov.InsereMovimentacao();

                alerta("Estoque atualizado com sucesso!");

            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void Buscar(String valor) {
        try(Connection conn = conexao.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM produto WHERE nome LIKE ? OR codigo_barras LIKE ?"
            )) {

            String busca = "%" + valor + "%";

            ps.setString(1, busca);
            ps.setString(2, busca);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                id = rs.getInt("id");
                nome = rs.getString("nome");
                codBarras = rs.getString("codigo_barras");
                descricao = rs.getString("descricao");
                categoria = rs.getString("categoria");
                quantidade = rs.getInt("quantidade");
                preco = rs.getDouble("preco");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    // ✅ MÉTODO QUE ESTAVA FALTANDO
    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
    public void estornarEstoque(int qtd) {

        try(Connection conn = conexao.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE produto SET quantidade = quantidade + ? WHERE id=?"
            );

            ps.setInt(1, qtd);
            ps.setInt(2, id);
            ps.executeUpdate();

            // 🔥 registra entrada
            MovimentacaoEstoqueModel mov = new MovimentacaoEstoqueModel(
                0, id, nome, null, qtd, "Entrada"
            );
            mov.InsereMovimentacao();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}