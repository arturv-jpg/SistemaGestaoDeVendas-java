package application.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import application.model.ClienteModel;
import application.model.ProdutoModel;
import application.model.UsuarioModel;
import application.model.VendaModel;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.geometry.Insets;

// 🔥 SERVICES
import service.VendaService;
import service.DescontoService;

public class VendaController implements Initializable {

    @FXML private TextField txtProduto, txtQtd;
    @FXML private TableView<ProdutoModel> tabItens;
    @FXML private TableColumn<ProdutoModel, String> colNome;
    @FXML private TableColumn<ProdutoModel, Integer> colQtd;
    @FXML private TableColumn<ProdutoModel, Double> colPreco;
    @FXML private ComboBox<ClienteModel> cbCliente;
    @FXML private Label lblTotal;

    private List<ProdutoModel> itens = new ArrayList<>();
    private double total = 0;
    private UsuarioModel usuarioLogado;
    private int ultimoIdVenda = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colNome.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNome()));
        colQtd.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getQuantidade()).asObject());
        colPreco.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getPreco()).asObject());
        carregarClientes();
    }

    private void carregarClientes() {
        List<ClienteModel> lista = ClienteModel.buscar("");
        cbCliente.setItems(FXCollections.observableArrayList(lista));

        cbCliente.setCellFactory(lv -> new ListCell<>() {
            protected void updateItem(ClienteModel c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty ? "" : c.getNome() + (c.getStatus().equals("Inativo") ? " (Inativo)" : ""));
            }
        });

        cbCliente.setButtonCell(new ListCell<>() {
            protected void updateItem(ClienteModel c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty ? "" : c.getNome());
            }
        });

        cbCliente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && "Inativo".equals(newVal.getStatus())) {
                alerta("Cliente inativo não pode realizar compras!");
                cbCliente.getSelectionModel().clearSelection();
            }
        });
    }

    @FXML
    public void adicionarItem() {
        if(txtProduto.getText().isEmpty() || txtQtd.getText().isEmpty()) {
            alerta("Informe produto e quantidade!");
            return;
        }

        ProdutoModel p = new ProdutoModel();
        p.Buscar(txtProduto.getText());

        if(p.getID() == 0) {
            alerta("Produto não encontrado!");
            return;
        }

        int qtd;
        try {
            qtd = Integer.parseInt(txtQtd.getText());
        } catch(Exception e) {
            alerta("Quantidade inválida!");
            return;
        }

        if(p.getQuantidade() <= 0) {
            alerta("Produto sem estoque!");
            return;
        }

        if(qtd > p.getQuantidade()) {
            alerta("Quantidade maior que o estoque disponível!");
            return;
        }

        ProdutoModel item = new ProdutoModel(
            p.getID(),
            p.getNome(),
            p.getCodBarras(),
            p.getDescricao(),
            p.getCategoria(),
            p.getPreco(),
            qtd
        );

        itens.add(item);
        total += p.getPreco() * qtd;

        atualizarTabela();
        lblTotal.setText("Total: R$ " + total);

        txtProduto.clear();
        txtQtd.clear();
    }

    private void atualizarTabela() {
        tabItens.setItems(FXCollections.observableArrayList(itens));
    }

    @FXML
    public void finalizarVenda() {

        ClienteModel cliente = cbCliente.getValue();

        if (cliente == null) {
            alerta("Selecione um cliente!");
            return;
        }

        if (itens.isEmpty()) {
            alerta("Adicione itens à venda!");
            return;
        }

        if ("Inativo".equals(cliente.getStatus())) {
            alerta("Cliente inativo não pode realizar compras!");
            return;
        }

        try {
            URL url = getClass().getResource("/application/view/PagamentoView.fxml");

            if (url == null) {
                alerta("Arquivo PagamentoView.fxml não encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            PagamentoController pagController = loader.getController();
            pagController.setTotal(total);
            pagController.setUsuarioLogado(usuarioLogado);

            Stage pagStage = new Stage();
            pagStage.setTitle("Pagamento");
            pagStage.setScene(new Scene(root));
            pagStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            pagStage.showAndWait();

            if (pagController.isAprovado()) {

                double descontoPercent = pagController.getDescontoPercent();
                double totalComDesconto = pagController.getTotalComDesconto();
                List<PagamentoController.PagamentoEntry> pagamentos = pagController.getPagamentos();
                double valorRecebidoDinheiro = pagController.getValorRecebidoDinheiro();
                double troco = pagController.getTroco();
                double totalPago = 0;

             // soma pagamentos
             for (PagamentoController.PagamentoEntry pag : pagamentos) {
                 totalPago += pag.getValor();
             }

             // soma dinheiro se houver
             totalPago += valorRecebidoDinheiro;

             // valida pagamento
             if (totalPago < totalComDesconto) {
                 alerta("Pagamento insuficiente! Total pago: R$ " + totalPago);
                 return;
             }
                DescontoService descontoService = new DescontoService();

                try {
                    if (descontoPercent > 5) {
                        Dialog<String[]> dialog = new Dialog<>();
                        dialog.setTitle("Autorização de Gerente");

                        Label lblLogin = new Label("Login:");
                        Label lblSenha = new Label("Senha:");

                        TextField txtLogin = new TextField();
                        PasswordField txtSenha = new PasswordField();

                        VBox vbox = new VBox(10, lblLogin, txtLogin, lblSenha, txtSenha);
                        dialog.getDialogPane().setContent(vbox);

                        ButtonType btnOk = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
                        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

                        dialog.setResultConverter(button -> {
                            if (button == btnOk) {
                                return new String[]{txtLogin.getText(), txtSenha.getText()};
                            }
                            return null;
                        });

                        dialog.showAndWait().ifPresent(result -> {

                            UsuarioModel gerente = UsuarioModel.autenticar(result[0], result[1]);

                            if (gerente == null || 
                                !(gerente.getTipo().equalsIgnoreCase("gerente") 
                                || gerente.getTipo().equalsIgnoreCase("admin"))) {

                                alerta("Apenas gerente pode autorizar desconto acima de 5%");
                                throw new RuntimeException(); // interrompe fluxo
                            }
                        });

                    }

                } catch (Exception e) {
                    return;
                }

                VendaModel venda = new VendaModel(cliente.getId(), totalComDesconto);

                int idVenda = venda.salvarVendaComPagamentos(
                        itens,
                        descontoPercent,
                        totalComDesconto,
                        pagamentos,
                        valorRecebidoDinheiro,
                        troco,
                        usuarioLogado.getId()
                );

                if (idVenda == 0) {
                    alerta("Erro ao finalizar venda! Verifique o estoque.");
                    return;
                }

                ultimoIdVenda = idVenda;

                alerta("Venda finalizada com sucesso! ID: " + idVenda);

                gerarComprovante(cliente, itens, total, descontoPercent,
                        totalComDesconto, pagamentos, valorRecebidoDinheiro, troco);

                itens.clear();
                total = 0;
                atualizarTabela();
                lblTotal.setText("Total: R$ 0.00");
                cbCliente.getSelectionModel().clearSelection();
            }

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Erro ao abrir tela de pagamento: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarVenda() {
        if (usuarioLogado == null || 
            (!usuarioLogado.getTipo().equalsIgnoreCase("admin") && 
             !usuarioLogado.getTipo().equalsIgnoreCase("gerente"))) {
            alerta("Apenas administradores ou gerentes podem cancelar vendas!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Venda");
        dialog.setHeaderText("Digite o ID da venda:");
        dialog.setContentText("ID:");

        dialog.showAndWait().ifPresent(idStr -> {
            try {
                int idVenda = Integer.parseInt(idStr);

                TextInputDialog motivoDialog = new TextInputDialog();
                motivoDialog.setTitle("Motivo");
                motivoDialog.setHeaderText("Informe o motivo do cancelamento:");

                motivoDialog.showAndWait().ifPresent(motivo -> {
                    VendaModel v = new VendaModel(0, 0);
                    boolean cancelado = v.cancelarVenda(idVenda, motivo, usuarioLogado.getId());

                    if (cancelado) {
                        if (idVenda == ultimoIdVenda) {
                            ultimoIdVenda = 0;
                        }

                        itens.clear();
                        total = 0;
                        atualizarTabela();
                        lblTotal.setText("Total: R$ 0.00");
                        cbCliente.getSelectionModel().clearSelection();
                    }
                });

            } catch (Exception e) {
                alerta("ID inválido!");
            }
        });
    }

    @FXML
    public void cancelarUltimaVenda() {
        if (ultimoIdVenda == 0) {
            alerta("Nenhuma venda recente para cancelar.");
            return;
        }

        if (usuarioLogado == null || 
            (!usuarioLogado.getTipo().equalsIgnoreCase("admin") && 
             !usuarioLogado.getTipo().equalsIgnoreCase("gerente"))) {
            alerta("Apenas administradores ou gerentes podem cancelar vendas!");
            return;
        }

        TextInputDialog motivoDialog = new TextInputDialog();
        motivoDialog.setTitle("Motivo");
        motivoDialog.setHeaderText("Informe o motivo do cancelamento:");

        motivoDialog.showAndWait().ifPresent(motivo -> {
            VendaModel v = new VendaModel(0, 0);
            boolean cancelado = v.cancelarVenda(ultimoIdVenda, motivo, usuarioLogado.getId());

            if (cancelado) {
                alerta("Venda cancelada com sucesso!");
                ultimoIdVenda = 0;

                itens.clear();
                total = 0;
                atualizarTabela();
                lblTotal.setText("Total: R$ 0.00");
                cbCliente.getSelectionModel().clearSelection();
            }
        });
    }

    private void gerarComprovante(ClienteModel cliente, List<ProdutoModel> itensVendidos, 
            double totalOriginal, double descontoPercent, double totalComDesconto,
            List<PagamentoController.PagamentoEntry> pagamentos, 
            double valorRecebidoDinheiro, double troco) {

        StringBuilder sb = new StringBuilder();
        sb.append("========== CUPOM NÃO FISCAL ==========\n");
        sb.append("Cliente: ").append(cliente.getNome()).append("\n\n");

        for (ProdutoModel p : itensVendidos) {
            sb.append(p.getNome()).append(" x").append(p.getQuantidade())
              .append(" - R$ ").append(p.getPreco() * p.getQuantidade()).append("\n");
        }

        sb.append("\nTotal: R$ ").append(totalComDesconto);

        Stage stage = new Stage();
        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);

        VBox vbox = new VBox(area);
        vbox.setPadding(new Insets(10));

        stage.setScene(new Scene(vbox, 400, 400));
        stage.setTitle("Comprovante");
        stage.show();
    }

    private void alerta(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }

    public void setUsuarioLogado(UsuarioModel usuario) {
        this.usuarioLogado = usuario;
    }
}