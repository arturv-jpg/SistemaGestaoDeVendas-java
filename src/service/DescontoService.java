package service;

public class DescontoService {

    // Regra: até 5% qualquer vendedor pode aplicar
    // Acima de 5% precisa ser gerente
    public void validarDesconto(double percentualDesconto, boolean isGerente) throws Exception {

        if (percentualDesconto < 0) {
            throw new Exception("Desconto não pode ser negativo.");
        }

        if (percentualDesconto > 5.0 && !isGerente) {
            throw new Exception("Desconto acima de 5% requer autorização do gerente.");
        }
    }

    // Método opcional para aplicar desconto no valor total
    public double aplicarDesconto(double valorTotal, double percentualDesconto) {
        double desconto = valorTotal * (percentualDesconto / 100);
        return valorTotal - desconto;
    }
}