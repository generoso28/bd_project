package br.inatel.DAO;

import br.inatel.Biblioteca.*;
import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a entidade mais complexa 'Exemplar_livro'.
 * Herda de AbstractComplexDao para reutilizar a lógica de processamento de ResultSet
 * e implementa a interface Dao para as operações de CRUD.
 */
public class ExemplarDao extends AbstractComplexDao<Exemplar_livro, Integer> implements Dao<Exemplar_livro, Integer> {
    private final Connection conn;

    public ExemplarDao(Connection connection) {
        this.conn = connection;
    }

    // readAll agora é simples e consistente.
    @Override
    public List<Exemplar_livro> readAll() throws SQLException {
        String sql = getCompletaQuery() + " ORDER BY e.id_livro, a.nome";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return processResultSet(rs);
        }
    }

    // read também é simplificado.
    @Override
    public Exemplar_livro read(Integer id) throws SQLException {
        String sql = getCompletaQuery() + " WHERE e.id_livro = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Exemplar_livro> exemplares = processResultSet(rs);
                return exemplares.isEmpty() ? null : exemplares.getFirst();
            }
        }
    }

    // MÉTODOS ABSTRATOS IMPLEMENTADOS:
    // Aqui fornecemos os "detalhes" que a classe abstrata precisa.

    @Override
    protected Integer getKeyFromResultSet(ResultSet rs) throws SQLException {
        return rs.getInt("id_livro");
    }

    @Override
    protected Exemplar_livro mapRowToMainEntity(ResultSet rs) throws SQLException {
        // 1. Cria a Categoria
        Categoria categoria = new Categoria(rs.getInt("idCategoria"), rs.getString("nomeCategoria"));

        // 2. Cria o Livro (com lista de autores vazia por enquanto)
        Livro livro = new Livro(
                rs.getString("isbn"),
                categoria,
                rs.getString("titulo"),
                rs.getInt("anoPublicacao"),
                new ArrayList<>()
        );

        // 3. Cria o Exemplar, que é a entidade principal
        return new Exemplar_livro(
                rs.getInt("id_livro"),
                livro,
                rs.getBoolean("status")
        );
    }

    @Override
    protected void addNestedEntity(Exemplar_livro exemplar, ResultSet rs) throws SQLException {
        // A entidade aninhada aqui é o Autor, que pertence ao Livro dentro do Exemplar.
        int autorId = rs.getInt("id");
        if (autorId != 0) {
            Livro livroDoExemplar = exemplar.getLivro();
            boolean autorJaAdicionado = livroDoExemplar.getAutores().stream().anyMatch(a -> a.getId() == autorId);
            if (!autorJaAdicionado) {
                Autor autor = new Autor(autorId, rs.getString("nome"), rs.getString("paisOrigem"));
                livroDoExemplar.addAutor(autor);
            }
        }
    }

    private String getCompletaQuery() {
        // A "Super Query" que busca tudo de uma vez.
        return """
        SELECT
            e.id_livro, e.status,
            l.isbn, l.titulo, l.anoPublicacao,
            c.idCategoria, c.nomeCategoria,
            a.id, a.nome, a.paisOrigem
        FROM exemplar_livro e
        JOIN livro l ON e.isbn_livro = l.isbn
        JOIN categoria c ON l.categoria_idCategoria = c.idCategoria
        LEFT JOIN livro_has_autor lha ON l.isbn = lha.livro_ISBN
        LEFT JOIN autor a ON lha.autor_idAutor = a.id
        """;
    }

    // Os métodos de escrita (create, update, delete) permanecem inalterados.
    @Override
    public boolean create(Exemplar_livro exemplar) throws SQLException {
        String sql = "INSERT INTO exemplar_livro (isbn_livro, status) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, exemplar.getLivro().getIsbn());
            stmt.setBoolean(2, exemplar.isStatus());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM exemplar_livro WHERE id_livro = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}