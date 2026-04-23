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
    
    // NOVOS ATRIBUTOS
    private double precoCusto;
    private double margemLucro;
    private int estoqueMinimo;

    // Construtor vazio
    public ProdutoModel() {}

    // Construtor completo (com novos campos)
    public ProdutoModel(int id, String nome, String codBarras, String descricao,
                        String categoria, double preco, int quantidade,
                        double precoCusto, double margemLucro, int estoqueMinimo) {
        this.id = id;
        this.nome = nome;
        this.codBarras = codBarras;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
        this.quantidade = quantidade;
        this.precoCusto = precoCusto;
        this.margemLucro = margemLucro;
        this.estoqueMinimo = estoqueMinimo;
    }
    
    // Construtor alternativo para compatibilidade com código antigo (sem os novos campos)
    public ProdutoModel(int id, String nome, String codBarras, String descricao,
                        String categoria, double preco, int quantidade) {
        this(id, nome, codBarras, descricao, categoria, preco, quantidade, 0.0, 0.0, 0);
    }

    // GETTERS
    public int getID() { return id; }
    public String getNome() { return nome; }
    public String getCodBarras() { return codBarras; }
    public String getDescricao() { return descricao; }
    public String getCategoria() { return categoria; }
    public double getPreco() { return preco; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoCusto() { return precoCusto; }
    public double getMargemLucro() { return margemLucro; }
    public int getEstoqueMinimo() { return estoqueMinimo; }

    // SETTERS
    public void setID(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setCodBarras(String codBarras) { this.codBarras = codBarras; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setPreco(double preco) { this.preco = preco; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public void setPrecoCusto(double precoCusto) { this.precoCusto = precoCusto; }
    public void setMargemLucro(double margemLucro) { this.margemLucro = margemLucro; }
    public void setEstoqueMinimo(int estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }
    
    // NOVO MÉTODO: calcular preço de venda com base no custo e margem
    public double calcularPrecoVenda() {
        return precoCusto * (1 + margemLucro / 100);
    }

    // SALVAR
    public void Salvar() {
        try(Connection conn = conexao.getConnection()) {

            if(this.id > 0) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE produto SET nome=?, codigo_barras=?, descricao=?, categoria=?, " +
                    "preco=?, quantidade=?, preco_custo=?, margem_lucro=?, estoque_minimo=? WHERE id=?"
                );

                ps.setString(1, nome);
                ps.setString(2, codBarras);
                ps.setString(3, descricao);
                ps.setString(4, categoria);
                ps.setDouble(5, preco);
                ps.setInt(6, quantidade);
                ps.setDouble(7, precoCusto);
                ps.setDouble(8, margemLucro);
                ps.setInt(9, estoqueMinimo);
                ps.setInt(10, id);
                ps.executeUpdate();

                alerta("Produto Alterado!");

            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO produto (nome, codigo_barras, descricao, categoria, preco, quantidade, " +
                    "preco_custo, margem_lucro, estoque_minimo) VALUES (?,?,?,?,?,?,?,?,?)"
                );

                ps.setString(1, nome);
                ps.setString(2, codBarras);
                ps.setString(3, descricao);
                ps.setString(4, categoria);
                ps.setDouble(5, preco);
                ps.setInt(6, quantidade);
                ps.setDouble(7, precoCusto);
                ps.setDouble(8, margemLucro);
                ps.setInt(9, estoqueMinimo);
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
                    rs.getInt("quantidade"),
                    rs.getDouble("preco_custo"),
                    rs.getDouble("margem_lucro"),
                    rs.getInt("estoque_minimo")
                ));
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // EXCLUIR (sem alterações)
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

 // Em ProdutoModel.java - altere o método ProcessaEstoque
    public void ProcessaEstoque(String operacao, int idUsuario) {
        if (id <= 0) return;
        try (Connection conn = conexao.getConnection()) {
            PreparedStatement check = conn.prepareStatement("SELECT quantidade FROM produto WHERE id=?");
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                int estoqueAtual = rs.getInt("quantidade");
                if (operacao.equals("Saida") && quantidade > estoqueAtual) {
                    alerta("Estoque insuficiente!");
                    return;
                }
                String sql = operacao.equals("Saida") ? "UPDATE produto SET quantidade = quantidade - ? WHERE id=?" : "UPDATE produto SET quantidade = quantidade + ? WHERE id=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, quantidade);
                ps.setInt(2, id);
                ps.execute();
                // Registra movimentação com idUsuario
                MovimentacaoEstoqueModel mov = new MovimentacaoEstoqueModel(0, id, nome, null, quantidade, operacao, idUsuario);
                mov.InsereMovimentacao(idUsuario);
                alerta("Estoque atualizado com sucesso!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Método estornarEstoque também precisa receber idUsuario
    public void estornarEstoque(int qtd, int idUsuario) {
        try (Connection conn = conexao.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE produto SET quantidade = quantidade + ? WHERE id=?");
            ps.setInt(1, qtd);
            ps.setInt(2, id);
            ps.executeUpdate();
            MovimentacaoEstoqueModel mov = new MovimentacaoEstoqueModel(0, id, nome, null, qtd, "Entrada", idUsuario);
            mov.InsereMovimentacao(idUsuario);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    // BUSCAR (atualizado para incluir novos campos)
    public boolean Buscar(String valor) {
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
                precoCusto = rs.getDouble("preco_custo");
                margemLucro = rs.getDouble("margem_lucro");
                estoqueMinimo = rs.getInt("estoque_minimo");
                return true;
            } else {
                id = 0;
                nome = null;
                codBarras = null;
                descricao = null;
                categoria = null;
                quantidade = 0;
                preco = 0;
                precoCusto = 0;
                margemLucro = 0;
                estoqueMinimo = 0;
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}