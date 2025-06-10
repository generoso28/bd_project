package br.inatel.DAO;

import br.inatel.Biblioteca.Autor;
import br.inatel.Biblioteca.Categoria;
import br.inatel.Biblioteca.Livro;
import br.inatel.Interfaces.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LivroDao implements Dao<Livro, String> {
    private Connection conn; // Conexão com o banco

    public LivroDao(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean create(Livro livro) throws SQLException {
        conn.setAutoCommit(false);
        String sqlLivro = "INSERT INTO livro (isbn, titulo, anoPublicacao, categoria_idCategoria) VALUES (?, ?, ?, ?)";
        String sqlAutores = "INSERT INTO livro_has_autor (livro_ISBN, autor_idAutor) VALUES (?, ?)";

        try (PreparedStatement stmtLivro = conn.prepareStatement(sqlLivro);
             PreparedStatement stmtAutores = conn.prepareStatement(sqlAutores)) {

            stmtLivro.setString(1, livro.getIsbn());
            stmtLivro.setString(2, livro.getTitulo());
            // CORREÇÃO: Passar o valor inteiro do ano para o PreparedStatement
            stmtLivro.setInt(3, livro.getAnoPublicacao().getValue());
            stmtLivro.setInt(4, livro.getCategoria().getId());
            int affectedRows = stmtLivro.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                return false; // Falha ao inserir o livro
            }

            for (Autor autor : livro.getAutores()) {
                stmtAutores.setString(1, livro.getIsbn());
                stmtAutores.setInt(2, autor.getId());
                stmtAutores.addBatch();
            }
            stmtAutores.executeBatch();

            conn.commit();
            return true; // SUCESSO: Retorna true

        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Erro ao criar livro. Transação revertida.");
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public Livro read(String isbn) throws SQLException {
        String sql = """
        SELECT 
            livro.isbn, 
            livro.titulo, 
            livro.anoPublicacao, 
            categoria.idCategoria,
            categoria.nomeCategoria,
            autor.idAutor,
            autor.nome,
            autor.paisOrigem
        FROM livro_has_autor
        JOIN autor ON livro_has_autor.autor_idAutor = autor.idAutor
        JOIN livro ON livro.isbn = livro_has_autor.livro_ISBN
        JOIN categoria ON livro.categoria_idCategoria = categoria.idCategoria
        WHERE livro.isbn = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            Livro livro = null;
            List<Autor> autores = new ArrayList<>();

            while (rs.next()) {
                if (livro == null) {
                    Categoria categoria = new Categoria(
                            rs.getInt("idCategoria"),
                            rs.getString("nomeCategoria")
                    );

                    livro = new Livro(
                            rs.getString("isbn"),
                            categoria,
                            rs.getString("titulo"),
                            Year.of(rs.getInt("anoPublicacao")),
                            autores
                    );
                }

                Autor autor = new Autor(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")
                );
                autores.add(autor);
            }

            return livro;
        }
    }

    @Override
    public List<Livro> readAll() throws SQLException {
        String sql = """
        SELECT
            l.isbn, l.titulo, l.anoPublicacao,
            c.idCategoria, c.nomeCategoria,
            a.idAutor, a.nome, a.paisOrigem
        FROM livro l
        JOIN categoria c ON l.categoria_idCategoria = c.idCategoria
        LEFT JOIN livro_has_autor lha ON l.isbn = lha.livro_ISBN
        LEFT JOIN autor a ON lha.autor_idAutor = a.idAutor
        ORDER BY l.titulo
    """;

        Map<String, Livro> livroMap = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");

                Livro livro = livroMap.get(isbn);
                if (livro == null) {
                    Categoria categoria = new Categoria(
                            rs.getInt("idCategoria"),
                            rs.getString("nomeCategoria")
                    );

                    livro = new Livro(
                            isbn,
                            categoria,
                            rs.getString("titulo"),
                            Year.of(rs.getInt("anoPublicacao")),
                            new ArrayList<>()
                    );

                    livroMap.put(isbn, livro);
                }

                Autor autor = new Autor(
                        rs.getInt("idAutor"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")  // Corrigido aqui
                );

                livro.addAutor(autor);
            }
        }

        return new ArrayList<>(livroMap.values());
    }

    @Override
    public boolean delete(String isbn) throws SQLException {
        conn.setAutoCommit(false);
        String sqlDeleteAutores = "DELETE FROM livro_has_autor WHERE livro_ISBN = ?";
        String sqlDeleteLivro = "DELETE FROM livro WHERE isbn = ?";

        try (PreparedStatement stmtAutores = conn.prepareStatement(sqlDeleteAutores);
             PreparedStatement stmtLivro = conn.prepareStatement(sqlDeleteLivro)) {

            stmtAutores.setString(1, isbn);
            stmtAutores.executeUpdate();

            stmtLivro.setString(1, isbn);
            int affectedRows = stmtLivro.executeUpdate();

            conn.commit();
            return affectedRows > 0;

        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Erro ao deletar livro. Transação revertida.");
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
