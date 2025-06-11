package br.inatel.DAO;

import br.inatel.Biblioteca.Usuario;
import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDao implements Dao<Usuario, Integer> {
    private final Connection conn;

    public UsuarioDao(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean create(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nome, telefone, email, tipo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getTelefone());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getTipo());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Usuario read(Integer id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE idUsuario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Os nomes das colunas aqui já estavam corretos
                return new Usuario(
                        rs.getInt("idUsuario"),
                        rs.getString("nome"),
                        rs.getString("telefone"),
                        rs.getString("email"),
                        rs.getString("tipo")
                );
            }
        }
        return null;
    }

    @Override
    public List<Usuario> readAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Os nomes das colunas aqui já estavam corretos
                usuarios.add(new Usuario(
                        rs.getInt("idUsuario"),
                        rs.getString("nome"),
                        rs.getString("telefone"),
                        rs.getString("email"),
                        rs.getString("tipo")
                ));
            }
        }
        return usuarios;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE idUsuario = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
