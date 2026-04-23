package service;

import java.util.List;

import application.model.MovimentacaoEstoqueModel;
import application.model.ProdutoModel;

public class VendaService {

    // RN02 - Bloqueio de venda sem estoque
    public void validarEstoque(List<ProdutoModel> produtos, List<Integer> quantidades) throws Exception {
        if (produtos == null || quantidades == null) {
            throw new Exception("Lista de produtos ou quantidades está nula.");
        }

        if (produtos.size() != quantidades.size()) {
            throw new Exception("Lista de produtos e quantidades incompatíveis.");
        }

        for (int i = 0; i < produtos.size(); i++) {
            ProdutoModel produto = produtos.get(i);
            int quantidadeSolicitada = quantidades.get(i);

            if (produto == null) {
                throw new Exception("Produto inválido.");
            }

            if (quantidadeSolicitada <= 0) {
                throw new Exception("Quantidade inválida para o produto: " + produto.getDescricao());
            }

            if (produto.getQuantidade() < quantidadeSolicitada) {
                throw new Exception(
                    "Estoque insuficiente para o produto: " + produto.getDescricao() +
                    " | Disponível: " + produto.getQuantidade()
                );
            }
        }
    }

    // RN01 - Baixa automática de estoque
    public void baixarEstoque(List<ProdutoModel> produtos, List<Integer> quantidades, int usuarioId) throws Exception {

        for (int i = 0; i < produtos.size(); i++) {

            ProdutoModel p = produtos.get(i);
            int qtd = quantidades.get(i);

            int novoEstoque = p.getQuantidade() - qtd;

            if (novoEstoque < 0) {
                throw new Exception("Estoque insuficiente para o produto: " + p.getNome());
            }

            p.setQuantidade(qtd);
            p.ProcessaEstoque("Saida", usuarioId);

            // 🔥 AQUI É O UPGRADE FINAL
            MovimentacaoEstoqueModel mov = new MovimentacaoEstoqueModel(
                0,
                p.getID(),
                p.getNome(),
                "",
                qtd,
                "Saida",
                usuarioId
            );

            mov.InsereMovimentacao(usuarioId);
        }
    }

    // RN03 - Estorno de estoque
    public void estornarEstoque(List<ProdutoModel> produtos, List<Integer> quantidades) {
        for (int i = 0; i < produtos.size(); i++) {
            ProdutoModel produto = produtos.get(i);
            int quantidade = quantidades.get(i);

            int novoEstoque = produto.getQuantidade() + quantidade;
            produto.setQuantidade(novoEstoque);
        }
    }
}