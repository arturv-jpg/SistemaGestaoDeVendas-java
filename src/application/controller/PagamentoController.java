package application.controller;

import java.util.ArrayList;
import java.util.List;

import application.model.UsuarioModel;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class PagamentoController {

    @FXML private Label lblTotal;
    @FXML private TextField txtDescontoPercent;
    @FXML private Label lblTotalComDesconto;
    @FXML private TableView<PagamentoEntry> tabelaPagamentos;
    @FXML private TableColumn<PagamentoEntry, String> colForma;
    @FXML private TableColumn<PagamentoEntry, Double> colValor;
    @FXML private TableColumn<PagamentoEntry, Void> colAcao;
    @FXML private TextField txtValorRecebido;
    @FXML private Label lblTroco;

    private double totalOriginal;
    private double totalComDesconto;
    private double descontoPercent;
    private ObservableList<PagamentoEntry> pagamentos = FXCollections.observableArrayList();
    private UsuarioModel usuarioLogado;
    private boolean aprovado = false;

    public void setTotal(double total) {
        this.totalOriginal = total;
        this.totalComDesconto = total;
        lblTotal.setText(String.format("R$ %.2f", total));
        lblTotalComDesconto.setText(String.format("R$ %.2f", total));
    }

    public void setUsuarioLogado(UsuarioModel usuario) {
        this.usuarioLogado = usuario;
    }

    @FXML
    public void initialize() {
        colForma.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getForma()));
        colValor.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getValor()).asObject());
        colValor.setCellFactory(col -> new TableCell<PagamentoEntry, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty ? null : String.format("R$ %.2f", valor));
            }
        });
        colAcao.setCellFactory(col -> new TableCell<PagamentoEntry, Void>() {
            private final Button btnRemover = new Button("Remover");
            {
                btnRemover.setOnAction(e -> {
                    PagamentoEntry entry = getTableView().getItems().get(getIndex());
                    pagamentos.remove(entry);
                    recalcularTroco();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemover);
            }
        });
        tabelaPagamentos.setItems(pagamentos);
        txtDescontoPercent.textProperty().addListener((obs, old, novo) -> aplicarDesconto());
        txtValorRecebido.textProperty().addListener((obs, old, novo) -> recalcularTroco());
    }

    private void aplicarDesconto() {
        try {
            descontoPercent = Double.parseDouble(txtDescontoPercent.getText().replace(",", "."));
            if (descontoPercent < 0) descontoPercent = 0;
            if (descontoPercent > 100) descontoPercent = 100;
            totalComDesconto = totalOriginal * (1 - descontoPercent / 100);
            lblTotalComDesconto.setText(String.format("R$ %.2f", totalComDesconto));
            recalcularTroco();
        } catch (NumberFormatException e) {
            descontoPercent = 0;
            totalComDesconto = totalOriginal;
            lblTotalComDesconto.setText(String.format("R$ %.2f", totalComDesconto));
        }
    }

    private void recalcularTroco() {
        double somaPagamentos = pagamentos.stream().mapToDouble(PagamentoEntry::getValor).sum();
        double valorRecebido = 0;
        try {
            valorRecebido = Double.parseDouble(txtValorRecebido.getText().replace(",", "."));
        } catch (Exception e) {}
        double totalPago = somaPagamentos + valorRecebido;
        double troco = totalPago - totalComDesconto;
        if (troco < 0) troco = 0;
        lblTroco.setText(String.format("R$ %.2f", troco));
    }

    @FXML
    private void adicionarPagamento() {

        ChoiceDialog<String> dialog = new ChoiceDialog<>("dinheiro", "dinheiro", "cartao_debito", "cartao_credito");
        dialog.setTitle("Forma de Pagamento");
        dialog.setHeaderText("Selecione a forma de pagamento:");
        dialog.setContentText("Forma:");

        dialog.showAndWait().ifPresent(forma -> {

            double valorRestante = totalComDesconto 
                    - pagamentos.stream().mapToDouble(PagamentoEntry::getValor).sum();

            if (valorRestante <= 0) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Pagamento já completo!");
                a.show();
                return;
            }

            // 🔥 CARTÃO NÃO PEDE VALOR
            if (forma.equals("cartao_debito") || forma.equals("cartao_credito")) {

                pagamentos.add(new PagamentoEntry(forma, valorRestante));
                recalcularTroco();
                return;
            }

            // 🔥 DINHEIRO CONTINUA NORMAL
            TextInputDialog valorDialog = new TextInputDialog();
            valorDialog.setTitle("Valor");
            valorDialog.setHeaderText("Informe o valor em dinheiro");
            valorDialog.setContentText("Valor (R$):");

            valorDialog.showAndWait().ifPresent(valStr -> {
                try {
                    double valor = Double.parseDouble(valStr.replace(",", "."));
                    if (valor <= 0) throw new NumberFormatException();

                    pagamentos.add(new PagamentoEntry(forma, valor));
                    recalcularTroco();

                } catch (NumberFormatException e) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setContentText("Valor inválido!");
                    a.show();
                }
            });
        });
    }

    @FXML
    private void finalizar() {
    	// Validação do desconto acima de 5% para vendedor
    	if (usuarioLogado != null && "vendedor".equalsIgnoreCase(usuarioLogado.getTipo())) {
    	    if (descontoPercent > 5) {
    	        TextInputDialog senhaDialog = new TextInputDialog();
    	        senhaDialog.setTitle("Autorização de Gerente");
    	        senhaDialog.setHeaderText("Desconto acima de 5% requer senha de gerente.");
    	        senhaDialog.setContentText("Senha do gerente:");
    	        senhaDialog.showAndWait().ifPresent(senha -> {
    	            UsuarioModel gerente = UsuarioModel.autenticarGerente(senha);
    	            if (gerente != null) {
    	                aprovado = true;
    	                fecharComSucesso();
    	            } else {
    	                Alert a = new Alert(Alert.AlertType.ERROR);
    	                a.setContentText("Senha inválida! Desconto não autorizado.");
    	                a.show();
    	            }
    	        });
    	        return; // aguarda a resposta do dialog
    	    }
    	}
    	aprovado = true;
    	// 🔥 VALIDAÇÃO DE PAGAMENTO
    	double somaPagamentos = pagamentos.stream().mapToDouble(PagamentoEntry::getValor).sum();
    	double valorRecebido = getValorRecebidoDinheiro();
    	double totalPago = somaPagamentos + valorRecebido;

    	if (totalPago < totalComDesconto) {
    	    Alert a = new Alert(Alert.AlertType.ERROR);
    	    a.setContentText("Pagamento insuficiente! Total pago: R$ " + totalPago);
    	    a.show();
    	    return;
    	}
    	fecharComSucesso();
    }
    private void fecharComSucesso() {
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancelar() {
        aprovado = false;
        Stage stage = (Stage) lblTotal.getScene().getWindow();
        stage.close();
    }

    public boolean isAprovado() { return aprovado; }
    public double getDescontoPercent() { return descontoPercent; }
    public double getTotalComDesconto() { return totalComDesconto; }
    public List<PagamentoEntry> getPagamentos() { return new ArrayList<>(pagamentos); }
    public double getValorRecebidoDinheiro() {
        try {
            return Double.parseDouble(txtValorRecebido.getText().replace(",", "."));
        } catch (Exception e) {
            return 0;
        }
    }
    public double getTroco() {
        return Double.parseDouble(lblTroco.getText().replace("R$ ", "").replace(",", "."));
    }

    // Classe interna para representar um pagamento
    public static class PagamentoEntry {
        private final String forma;
        private final double valor;
        public PagamentoEntry(String forma, double valor) { this.forma = forma; this.valor = valor; }
        public String getForma() { return forma; }
        public double getValor() { return valor; }
    }
}