package application.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import application.conexao;

public class ClienteModel {

    private int id;
    private String nome;
    private String cpf;  
    private String email;
    private String status;

    // CONSTRUTOR
    public ClienteModel(int id, String nome, String cpf, String email, String status) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.status = status;
    }

    // GETTERS
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }

    // ================= VALIDAÇÃO DE CPF (COMPLETA) =================
    public static boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");
        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * (10 - i);
            }
            int dig1 = 11 - (soma % 11);
            if (dig1 >= 10) dig1 = 0;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * (11 - i);
            }
            int dig2 = 11 - (soma % 11);
            if (dig2 >= 10) dig2 = 0;

            return (dig1 == (cpf.charAt(9) - '0') && dig2 == (cpf.charAt(10) - '0'));
        } catch (Exception e) {
            return false;
        }
    }

    // ================= VALIDAÇÃO DE CNPJ (COMPLETA) =================
    public static boolean validarCNPJ(String cnpj) {
        cnpj = cnpj.replaceAll("[^0-9]", "");
        if (cnpj.length() != 14) return false;
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            int soma = 0;
            int peso = 5;
            for (int i = 0; i < 12; i++) {
                soma += (cnpj.charAt(i) - '0') * peso;
                peso = (peso == 2) ? 9 : peso - 1;
            }
            int dig1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

            soma = 0;
            peso = 6;
            for (int i = 0; i < 13; i++) {
                soma += (cnpj.charAt(i) - '0') * peso;
                peso = (peso == 2) ? 9 : peso - 1;
            }
            int dig2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

            return (dig1 == (cnpj.charAt(12) - '0') && dig2 == (cnpj.charAt(13) - '0'));
        } catch (Exception e) {
            return false;
        }
    }

    // ================= MÉTODO GENÉRICO =================
    public static boolean validarDocumento(String documento) {
        documento = documento.replaceAll("[^0-9]", "");
        if (documento.length() == 11) {
            return validarCPF(documento);
        } else if (documento.length() == 14) {
            return validarCNPJ(documento);
        } else {
            return false;
        }
    }

    // ================= SALVAR CLIENTE (com documento limpo) =================
    public boolean salvar() {
        try (Connection conn = conexao.getConnection()) {

            // Remove máscara do documento (CPF ou CNPJ)
            String documentoLimpo = this.cpf.replaceAll("[^0-9]", "");

            if (this.id > 0) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE cliente SET nome=?, cpf=?, email=?, status=? WHERE id=?"
                );
                ps.setString(1, nome.trim());
                ps.setString(2, documentoLimpo);
                ps.setString(3, email.trim());
                ps.setString(4, status != null ? status.trim() : "");
                ps.setInt(5, id);
                ps.executeUpdate();
                return true;
            } else {
                // Verifica duplicidade usando documento limpo
                PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM cliente WHERE cpf=? OR email=?"
                );
                check.setString(1, documentoLimpo);
                check.setString(2, email.trim());
                ResultSet rs = check.executeQuery();
                if (rs.next()) return false; // já existe

                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO cliente (nome, cpf, email, status) VALUES (?,?,?,?)"
                );
                ps.setString(1, nome.trim());
                ps.setString(2, documentoLimpo);
                ps.setString(3, email.trim());
                ps.setString(4, status != null ? status.trim() : "");
                ps.executeUpdate();
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= EXCLUIR =================
    public boolean excluir() {
        try (Connection conn = conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM cliente WHERE id=?")) {
            ps.setInt(1, this.id);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= BUSCAR CLIENTES =================
    public static List<ClienteModel> buscar(String valor) {
        List<ClienteModel> lista = new ArrayList<>();
        try (Connection conn = conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM cliente WHERE nome LIKE ? OR cpf LIKE ? OR email LIKE ?")) {
            String busca = "%" + valor.trim() + "%";
            ps.setString(1, busca);
            ps.setString(2, busca);
            ps.setString(3, busca);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new ClienteModel(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("cpf"),
                    rs.getString("email"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ================= HISTÓRICO (últimas 5 compras) =================
    public static List<String> historico(int idCliente) {
        List<String> lista = new ArrayList<>();
        try (Connection conn = conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT data, valor FROM venda WHERE idCliente=? ORDER BY data DESC LIMIT 5")) {
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add("Data: " + rs.getString("data") + " | Valor: R$ " + rs.getDouble("valor"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}