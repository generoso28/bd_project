package br.inatel.DAO;

import br.inatel.Biblioteca.Autor;
import br.inatel.Interfaces.Dao; // Garanta que o pacote da interface está correto

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AutorDao implements Dao<Autor, Integer> {
    private Connection conn;

    public AutorDao(Connection connection) {
        this.conn = connection;
    }
    @Override
    public boolean create(Autor autor) throws SQLException {
        String sql = "INSERT INTO autor (nome, paisOrigem) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, autor.getNome());
            stmt.setString(2, autor.getNacionalidade());
            return stmt.executeUpdate() > 0;
        }
    }
    @Override
    public Autor read(Integer id) throws SQLException {
        String sql = "SELECT * FROM autor WHERE idAutor = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Os nomes das colunas aqui já estavam corretos
                return new Autor(
                        rs.getInt("idAutor"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")
                );
            }
        }
        return null;
    }

    @Override
    public List<Autor> readAll() throws SQLException {
        List<Autor> autores = new ArrayList<>();
        String sql = "SELECT * FROM autor";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Os nomes das colunas aqui já estavam corretos
                autores.add(new Autor(
                        rs.getInt("idAutor"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")
                ));
            }
        }
        return autores;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM autor WHERE idAutor = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}