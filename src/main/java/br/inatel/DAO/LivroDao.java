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
        Livro livro = new Livro(
                rs.getString("isbn"),
                categoria,
                rs.getString("titulo"),
                rs.getInt("anoPublicacao"),
                new ArrayList<>() // A lista de autores começa vazia
        );
        livro.setQuantidadeExemplares(rs.getInt("quantidadeExemplares"));
        return livro;
    }

    @Override
    protected void addNestedEntity(Livro livro, ResultSet rs) throws SQLException {
        // Adiciona o autor (a entidade aninhada) se ele existir na linha atual.
        int autorId = rs.getInt("id");
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
            a.id, a.nome, a.paisOrigem,
            (SELECT COUNT(*) FROM exemplar_livro ex WHERE ex.isbn_livro = l.isbn) AS quantidadeExemplares
        FROM livro l
        JOIN categoria c ON l.categoria_idCategoria = c.idCategoria
        LEFT JOIN livro_has_autor lha ON l.isbn = lha.livro_ISBN
        LEFT JOIN autor a ON lha.autor_idAutor = a.id
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
            stmtLivro.setInt(3, livro.getAnoPublicacao());
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
        conn.setAutoCommit(false); // Inicia a transação

        // 1. SQL para deletar os exemplares (NOVO)
        String sqlDeleteExemplares = "DELETE FROM exemplar_livro WHERE isbn_livro = ?";
        // 2. SQL para deletar as associações com autores
        String sqlDeleteAutores = "DELETE FROM livro_has_autor WHERE livro_ISBN = ?";
        // 3. SQL para deletar o livro principal
        String sqlDeleteLivro = "DELETE FROM livro WHERE isbn = ?";

        // Usamos um bloco try-with-resources para garantir que todos os Statements sejam fechados
        try (PreparedStatement stmtExemplares = conn.prepareStatement(sqlDeleteExemplares); // NOVO
             PreparedStatement stmtAutores = conn.prepareStatement(sqlDeleteAutores);
             PreparedStatement stmtLivro = conn.prepareStatement(sqlDeleteLivro)) {

            // --- Ordem de execução é importante por causa das chaves estrangeiras ---

            // Passo 1: Deleta os exemplares associados ao livro
            stmtExemplares.setString(1, isbn);
            stmtExemplares.executeUpdate();

            // Passo 2: Deleta as associações na tabela livro_has_autor
            stmtAutores.setString(1, isbn);
            stmtAutores.executeUpdate();

            // Passo 3: Finalmente, deleta o livro
            stmtLivro.setString(1, isbn);
            int affectedRows = stmtLivro.executeUpdate(); // O resultado final depende desta operação

            conn.commit(); // Se tudo deu certo, confirma a transação
            return affectedRows > 0;

        } catch (SQLException e) {
            conn.rollback(); // Se qualquer passo falhar, desfaz todas as operações
            System.err.println("Erro ao deletar livro. A transação foi revertida.");
            throw e; // Lança a exceção para a camada superior (o Menu) tratar
        } finally {
            conn.setAutoCommit(true); // Restaura o modo padrão de auto-commit
        }
    }
}