package br.inatel.Biblioteca;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Autor {
    private int id;
    private String nome;
    private String nacionalidade;

    public Autor(int id, String nome, String nacionalidade) {
        this.id = id;
        this.nome = nome;
        this.nacionalidade = nacionalidade;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public static int create(Connection conn, Autor autor) throws SQLException {
        String sql = "INSERT INTO autor (nome, paisOrigem) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, autor.nome);
            stmt.setString(2, autor.nacionalidade);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public static Autor read(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM autor WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Autor(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")
                );
            }
        }
        return null;
    }
    public static List<Autor> readAll(Connection conn) throws SQLException {
        List<Autor> autores = new ArrayList<>();
        String sql = "SELECT * FROM autor";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                autores.add(new Autor(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")
                ));
            }
        }
        return autores;
    }
    public static void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM autor WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
