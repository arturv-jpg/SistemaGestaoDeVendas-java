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

   
    //  VALIDAÇÃO DE CPF
   
    public static boolean validarCPF(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");

        if (cpf.length() != 11) return false;

        // evita CPF tipo 11111111111
        if (cpf.matches("(\\d)\\1{10}")) return false;

        return true;
    }

   
    //  SALVAR CLIENTE
   
    public boolean salvar() {
        try (Connection conn = conexao.getConnection()) {

            //  UPDATE 
            if (this.id > 0) {

                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE cliente SET nome=?, cpf=?, email=?, status=? WHERE id=?"
                );

                ps.setString(1, nome.trim());
                ps.setString(2, cpf.trim());
                ps.setString(3, email.trim());
                ps.setString(4, status != null ? status.trim() : "");
                ps.setInt(5, id);

                ps.executeUpdate();
                return true;
            }

            //  INSERT 
            PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM cliente WHERE cpf=? OR email=?"
            );

            check.setString(1, cpf.trim());
            check.setString(2, email.trim());

            ResultSet rs = check.executeQuery();

            if (rs.next()) return false;

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO cliente (nome, cpf, email, status) VALUES (?,?,?,?)"
            );

            ps.setString(1, nome.trim());
            ps.setString(2, cpf.trim());
            ps.setString(3, email.trim());
            ps.setString(4, status != null ? status.trim() : "");

            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean excluir() {
        try (Connection conn = conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM cliente WHERE id=?")) {

            ps.setInt(1, this.id);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    //  BUSCAR CLIENTES
   
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

    
    // HISTÓRICO (últimas 5 compras)
    public static List<String> historico(int idCliente) {

        List<String> lista = new ArrayList<>();

        try (Connection conn = conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT data, valor FROM venda WHERE idCliente=? ORDER BY data DESC LIMIT 5")) {

            ps.setInt(1, idCliente);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(
                    "Data: " + rs.getString("data") +
                    " | Valor: R$ " + rs.getDouble("valor")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}