package br.inatel.DAO;

import br.inatel.Biblioteca.Autor;
import br.inatel.Biblioteca.Categoria;
import br.inatel.Biblioteca.Livro;
import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a entidade complexa 'Livro'.
 * Herda de AbstractComplexDao para reutilizar a lógica de processamento de ResultSet
 * e implementa a interface Dao para as operações de CRUD.
 */
public class LivroDao extends AbstractComplexDao<Livro, String> implements Dao<Livro, String> {
    private final Connection conn;

    public LivroDao(Connection conn) {
        this.conn = conn;
    }

    // readAll agora é extremamente simples.
    @Override
    public List<Livro> readAll() throws SQLException {
        String sql = getCompletaQuery() + " ORDER BY l.titulo, a.nome";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Delega o trabalho pesado para a classe pai.
            return processResultSet(rs);
        }
    }

    // read também é simplificado.
    @Override
    public Livro read(String isbn) throws SQLException {
        String sql = getCompletaQuery() + " WHERE l.isbn = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Livro> livros = processResultSet(rs);
                return livros.isEmpty() ? null : livros.getFirst();
            }
        }
    }

    // MÉTODOS ABSTRATOS IMPLEMENTADOS:
    // Aqui fornecemos os "detalhes" que a classe abstrata precisa.

    @Override
    protected String getKeyFromResultSet(ResultSet rs) throws SQLException {
        return rs.getString("isbn");
    }

    @Override
    protected Livro mapRowToMainEntity(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(rs.getInt("idCategoria"), rs.getString("nomeCategoria"));
        return new Livro(
                rs.getString("isbn"),
                categoria,
                rs.getString("titulo"),
                Year.of(rs.getInt("anoPublicacao")),
                new ArrayList<>() // A lista de autores começa vazia
        );
    }

    @Override
    protected void addNestedEntity(Livro livro, ResultSet rs) throws SQLException {
        // Adiciona o autor (a entidade aninhada) se ele existir na linha atual.
        int autorId = rs.getInt("idAutor");
        if (autorId != 0) {
            // Garante que o mesmo autor não seja adicionado múltiplas vezes ao mesmo livro.
            boolean autorJaAdicionado = livro.getAutores().stream().anyMatch(a -> a.getId() == autorId);
            if (!autorJaAdicionado) {
                Autor autor = new Autor(autorId, rs.getString("nome"), rs.getString("paisOrigem"));
                livro.addAutor(autor);
            }
        }
    }

    private String getCompletaQuery() {
        return """
        SELECT
            l.isbn, l.titulo, l.anoPublicacao,
            c.idCategoria, c.nomeCategoria,
            a.idAutor, a.nome, a.paisOrigem
        FROM livro l
        JOIN categoria c ON l.categoria_idCategoria = c.idCategoria
        LEFT JOIN livro_has_autor lha ON l.isbn = lha.livro_ISBN
        LEFT JOIN autor a ON lha.autor_idAutor = a.idAutor
        """;
    }

    // Os métodos de escrita (create, update, delete) não mudam.
    @Override
    public boolean create(Livro livro) throws SQLException {
        conn.setAutoCommit(false);
        String sqlLivro = "INSERT INTO livro (isbn, titulo, anoPublicacao, categoria_idCategoria) VALUES (?, ?, ?, ?)";
        String sqlAutores = "INSERT INTO livro_has_autor (livro_ISBN, autor_idAutor) VALUES (?, ?)";

        try (PreparedStatement stmtLivro = conn.prepareStatement(sqlLivro);
             PreparedStatement stmtAutores = conn.prepareStatement(sqlAutores)) {

            stmtLivro.setString(1, livro.getIsbn());
            stmtLivro.setString(2, livro.getTitulo());
            stmtLivro.setInt(3, livro.getAnoPublicacao().getValue());
            stmtLivro.setInt(4, livro.getCategoria().getId());
            int affectedRows = stmtLivro.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            for (Autor autor : livro.getAutores()) {
                stmtAutores.setString(1, livro.getIsbn());
                stmtAutores.setInt(2, autor.getId());
                stmtAutores.addBatch();
            }
            stmtAutores.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
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
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}