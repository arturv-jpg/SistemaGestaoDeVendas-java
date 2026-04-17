package application.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import application.conexao;
import javafx.scene.control.Alert;

public class VendaModel {

    private int id;
    private int idCliente;
    private double valor;

    public VendaModel(int idCliente, double valor) {
        this.idCliente = idCliente;
        this.valor = valor;
    }

    public boolean salvarVenda(List<ProdutoModel> itens) {

        Connection conn = null;

        try {
            conn = conexao.getConnection();
            conn.setAutoCommit(false);

            // 🔹 INSERE VENDA
            PreparedStatement vendaPS = conn.prepareStatement(
                "INSERT INTO venda (idCliente, data, valor, status) VALUES (?, NOW(), ?, 'FINALIZADA')",
                PreparedStatement.RETURN_GENERATED_KEYS
            );

            vendaPS.setInt(1, idCliente);
            vendaPS.setDouble(2, valor);
            vendaPS.executeUpdate();

            ResultSet rs = vendaPS.getGeneratedKeys();
            rs.next();
            int idVenda = rs.getInt(1);

            // 🔹 ITENS
            for (ProdutoModel p : itens) {

                // 🔻 verifica estoque
                PreparedStatement check = conn.prepareStatement(
                    "SELECT quantidade FROM produto WHERE id=?"
                );
                check.setInt(1, p.getID());
                ResultSet rsEstoque = check.executeQuery();

                if (!rsEstoque.next()) {
                    conn.rollback();
                    return false;
                }

                int estoqueAtual = rsEstoque.getInt("quantidade");

                if (estoqueAtual < p.getQuantidade()) {
                    conn.rollback();

                    alerta("Estoque insuficiente: " + p.getNome());
                    return false;
                }

                // item
                PreparedStatement itemPS = conn.prepareStatement(
                    "INSERT INTO item_venda (idVenda, idProduto, quantidade, preco) VALUES (?,?,?,?)"
                );

                itemPS.setInt(1, idVenda);
                itemPS.setInt(2, p.getID());
                itemPS.setInt(3, p.getQuantidade());
                itemPS.setDouble(4, p.getPreco());
                itemPS.executeUpdate();

                // baixa estoque
                PreparedStatement baixa = conn.prepareStatement(
                    "UPDATE produto SET quantidade = quantidade - ? WHERE id=?"
                );

                baixa.setInt(1, p.getQuantidade());
                baixa.setInt(2, p.getID());
                baixa.executeUpdate();

                // histórico saída
                PreparedStatement mov = conn.prepareStatement(
                    "INSERT INTO movimentacaoEstoque (idProduto, dataHora, quantidade, tipo) VALUES (?,NOW(),?,1)"
                );

                mov.setInt(1, p.getID());
                mov.setInt(2, p.getQuantidade());
                mov.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {}

            e.printStackTrace();
        }

        return false;
    }

    // 🚀 CANCELAMENTO COMPLETO (RN03)
    public boolean cancelarVenda(int idVenda, String motivo) {

        Connection conn = null;

        try {
            conn = conexao.getConnection();
            conn.setAutoCommit(false);

            // 🔍 verifica status
            PreparedStatement check = conn.prepareStatement(
                "SELECT status FROM venda WHERE id=?"
            );
            check.setInt(1, idVenda);

            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                alerta("Venda não encontrada!");
                return false;
            }

            if ("CANCELADA".equals(rs.getString("status"))) {
                alerta("Venda já cancelada!");
                return false;
            }

            // 🔹 buscar itens
            PreparedStatement itensPS = conn.prepareStatement(
                "SELECT idProduto, quantidade FROM item_venda WHERE idVenda=?"
            );
            itensPS.setInt(1, idVenda);

            ResultSet itens = itensPS.executeQuery();

            while (itens.next()) {

                int idProduto = itens.getInt("idProduto");
                int qtd = itens.getInt("quantidade");

                // 🔺 devolver estoque
                PreparedStatement devolve = conn.prepareStatement(
                    "UPDATE produto SET quantidade = quantidade + ? WHERE id=?"
                );

                devolve.setInt(1, qtd);
                devolve.setInt(2, idProduto);
                devolve.executeUpdate();

                // 🔥 histórico entrada
                PreparedStatement mov = conn.prepareStatement(
                    "INSERT INTO movimentacaoEstoque (idProduto, dataHora, quantidade, tipo) VALUES (?,NOW(),?,0)"
                );

                mov.setInt(1, idProduto);
                mov.setInt(2, qtd);
                mov.executeUpdate();
            }

            // 🔹 atualizar venda
            PreparedStatement update = conn.prepareStatement(
                "UPDATE venda SET status='CANCELADA', motivo_cancelamento=? WHERE id=?"
            );

            update.setString(1, motivo);
            update.setInt(2, idVenda);
            update.executeUpdate();

            conn.commit();

            alerta("Venda cancelada com sucesso!");
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {}

            e.printStackTrace();
        }

        return false;
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }
}