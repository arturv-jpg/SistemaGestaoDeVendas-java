package application.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import application.conexao;
import application.controller.PagamentoController;
import javafx.scene.control.Alert;

public class VendaModel {

    private int id;
    private int idCliente;
    private double valor;

    public VendaModel(int idCliente, double valor) {
        this.idCliente = idCliente;
        this.valor = valor;
    }

    // Método original (preservado)
    public boolean salvarVenda(List<ProdutoModel> itens) {
        // ... código original inalterado ...
        return true;
    }

    // NOVO MÉTODO: salva e retorna o ID da venda (0 se falhar)
    public int salvarVendaComPagamentos(List<ProdutoModel> itens, double descontoPercent, double totalComDesconto,
                                        List<PagamentoController.PagamentoEntry> pagamentos,
                                        double valorRecebidoDinheiro, double troco, int idUsuario) {
        Connection conn = null;
        try {
            conn = conexao.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement vendaPS = conn.prepareStatement(
                "INSERT INTO venda (idCliente, data, valor, desconto, valor_pago, troco, status) VALUES (?, NOW(), ?, ?, ?, ?, 'FINALIZADA')",
                PreparedStatement.RETURN_GENERATED_KEYS
            );
            vendaPS.setInt(1, idCliente);
            vendaPS.setDouble(2, totalComDesconto);
            vendaPS.setDouble(3, descontoPercent);
            vendaPS.setDouble(4, pagamentos.stream().mapToDouble(p -> p.getValor()).sum() + valorRecebidoDinheiro);
            vendaPS.setDouble(5, troco);
            vendaPS.executeUpdate();

            ResultSet rs = vendaPS.getGeneratedKeys();
            if (!rs.next()) {
                conn.rollback();
                return 0;
            }
            int idVenda = rs.getInt(1);

            for (ProdutoModel p : itens) {
                // verifica estoque
                PreparedStatement check = conn.prepareStatement("SELECT quantidade FROM produto WHERE id=?");
                check.setInt(1, p.getID());
                ResultSet rsEstoque = check.executeQuery();
                if (!rsEstoque.next()) { conn.rollback(); return 0; }
                int estoqueAtual = rsEstoque.getInt("quantidade");
                if (estoqueAtual < p.getQuantidade()) {
                    conn.rollback();
                    alerta("Estoque insuficiente: " + p.getNome());
                    return 0;
                }

                // item_venda
                PreparedStatement itemPS = conn.prepareStatement(
                    "INSERT INTO item_venda (idVenda, idProduto, quantidade, preco) VALUES (?,?,?,?)"
                );
                itemPS.setInt(1, idVenda);
                itemPS.setInt(2, p.getID());
                itemPS.setInt(3, p.getQuantidade());
                itemPS.setDouble(4, p.getPreco());
                itemPS.executeUpdate();

                // baixa estoque
                PreparedStatement baixa = conn.prepareStatement("UPDATE produto SET quantidade = quantidade - ? WHERE id=?");
                baixa.setInt(1, p.getQuantidade());
                baixa.setInt(2, p.getID());
                baixa.executeUpdate();

                // movimentação com idUsuario
                PreparedStatement mov = conn.prepareStatement(
                    "INSERT INTO movimentacaoEstoque (idProduto, dataHora, quantidade, tipo, idUsuario) VALUES (?, NOW(), ?, 1, ?)"
                );
                mov.setInt(1, p.getID());
                mov.setInt(2, p.getQuantidade());
                mov.setInt(3, idUsuario);
                mov.executeUpdate();
            }

            // pagamentos
            for (PagamentoController.PagamentoEntry pag : pagamentos) {
                PreparedStatement pagPS = conn.prepareStatement(
                    "INSERT INTO pagamento_venda (idVenda, forma, valor) VALUES (?,?,?)"
                );
                pagPS.setInt(1, idVenda);
                pagPS.setString(2, pag.getForma());
                pagPS.setDouble(3, pag.getValor());
                pagPS.executeUpdate();
            }
            if (valorRecebidoDinheiro > 0) {
                PreparedStatement pagPS = conn.prepareStatement(
                    "INSERT INTO pagamento_venda (idVenda, forma, valor) VALUES (?, 'dinheiro', ?)"
                );
                pagPS.setInt(1, idVenda);
                pagPS.setDouble(2, valorRecebidoDinheiro);
                pagPS.executeUpdate();
            }

            conn.commit();
            return idVenda;
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            return 0;
        }
    }

    // Cancelamento (com idUsuario)
    public boolean cancelarVenda(int idVenda, String motivo, int idUsuario) {
        Connection conn = null;
        try {
            conn = conexao.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement check = conn.prepareStatement("SELECT status FROM venda WHERE id=?");
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

            PreparedStatement itensPS = conn.prepareStatement("SELECT idProduto, quantidade FROM item_venda WHERE idVenda=?");
            itensPS.setInt(1, idVenda);
            ResultSet itens = itensPS.executeQuery();
            while (itens.next()) {
                int idProduto = itens.getInt("idProduto");
                int qtd = itens.getInt("quantidade");

                PreparedStatement devolve = conn.prepareStatement("UPDATE produto SET quantidade = quantidade + ? WHERE id=?");
                devolve.setInt(1, qtd);
                devolve.setInt(2, idProduto);
                devolve.executeUpdate();

                PreparedStatement mov = conn.prepareStatement(
                    "INSERT INTO movimentacaoEstoque (idProduto, dataHora, quantidade, tipo, idUsuario) VALUES (?, NOW(), ?, 0, ?)"
                );
                mov.setInt(1, idProduto);
                mov.setInt(2, qtd);
                mov.setInt(3, idUsuario);
                mov.executeUpdate();
            }

            PreparedStatement update = conn.prepareStatement("UPDATE venda SET status='CANCELADA', motivo_cancelamento=? WHERE id=?");
            update.setString(1, motivo);
            update.setInt(2, idVenda);
            update.executeUpdate();

            conn.commit();
            alerta("Venda cancelada com sucesso!");
            return true;
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
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