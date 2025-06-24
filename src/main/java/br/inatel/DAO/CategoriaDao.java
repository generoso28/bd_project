package br.inatel.DAO;

import br.inatel.Biblioteca.Categoria;
import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao implements Dao<Categoria, Integer> {
    private Connection conn;

    public CategoriaDao(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean create(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nomeCategoria) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoria.getNome());
            ResultSet rs = stmt.getGeneratedKeys();
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Categoria read(Integer id) throws SQLException {
        String sql = "SELECT * FROM categoria WHERE idCategoria = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Categoria(
                        rs.getInt("idCategoria"),
                        rs.getString("nomeCategoria")
                );
            }
        }
        return null;
    }

    @Override
    public List<Categoria> readAll() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categoria";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categorias.add(new Categoria(
                        rs.getInt("idCategoria"),
                        rs.getString("nomeCategoria")
                ));
            }
        }
        return categorias;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE idCategoria = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}
