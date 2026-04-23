package application.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import application.conexao;

public class UsuarioModel {

    private int id;
    private String nome;
    private String login;
    private String senha;
    private String tipo;

    public UsuarioModel(int id, String nome, String login, String senha, String tipo) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.tipo = tipo;
    }

    // GETTERS
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public String getTipo() { return tipo; }

    // SALVAR USUÁRIO
    public void salvar() {
        try(Connection conn = conexao.getConnection();
            PreparedStatement ps = conn.prepareStatement(
            "insert into usuario (nome, login, senha, tipo) values (?,?,?,?)")) {

            ps.setString(1, nome.trim());
            ps.setString(2, login.trim());
            ps.setString(3, senha.trim());
            ps.setString(4, tipo.trim());
            ps.executeUpdate();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // LOGIN
    public static UsuarioModel autenticar(String login, String senha) {
        try(Connection conn = conexao.getConnection();
            PreparedStatement ps = conn.prepareStatement(
            "select * from usuario where login=? and senha=?")) {

            System.out.println("Tentando login: " + login + " / " + senha);

            ps.setString(1, login.trim());
            ps.setString(2, senha.trim());

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                System.out.println("USUÁRIO ENCONTRADO!");

                return new UsuarioModel(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("login"),
                    rs.getString("senha"),
                    rs.getString("tipo")
                );
            } else {
                System.out.println("LOGIN NÃO ENCONTRADO");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }
 
    public static UsuarioModel autenticarGerente(String senha) {
        try (Connection conn = conexao.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT * FROM usuario WHERE tipo = 'gerente' AND senha = ?")) {
            ps.setString(1, senha);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new UsuarioModel(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("login"),
                    rs.getString("senha"),
                    rs.getString("tipo")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}