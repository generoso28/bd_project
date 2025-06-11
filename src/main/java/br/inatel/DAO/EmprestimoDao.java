package br.inatel.DAO;

import br.inatel.Biblioteca.*;
import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a entidade complexa 'Emprestimo'.
 * Herda de AbstractComplexDao para reutilizar a lógica de processamento de ResultSet
 * e implementa a interface Dao para as operações de CRUD.
 */
public class EmprestimoDao extends AbstractComplexDao<Emprestimo, Integer> implements Dao<Emprestimo, Integer> {
    private final Connection conn;

    public EmprestimoDao(Connection connection) {
        this.conn = connection;
    }

    // O metodo readAll agora é muito mais simples!
    @Override
    public List<Emprestimo> readAll() throws SQLException {
        String sql = getCompletaQuery() + " ORDER BY em.data_emprestimo DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Chama o metodo da classe pai para fazer o trabalho pesado.
            return processResultSet(rs);
        }
    }

    // O metodo read também é simplificado.
    @Override
    public Emprestimo read(Integer id) throws SQLException {
        String sql = getCompletaQuery() + " WHERE em.id_emprestimo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Emprestimo> emprestimos = processResultSet(rs);
                return emprestimos.isEmpty() ? null : emprestimos.getFirst();
            }
        }
    }

    // MÉTODOS ABSTRATOS IMPLEMENTADOS:
    // Aqui fornecemos os detalhes que a AbstractComplexDao precisa.

    @Override
    protected Integer getKeyFromResultSet(ResultSet rs) throws SQLException {
        return rs.getInt("idEmprestimo");
    }

    @Override
    protected Emprestimo mapRowToMainEntity(ResultSet rs) throws SQLException {
        // 1. Cria o Usuário
        Usuario usuario = new Usuario(
                rs.getInt("idUsuario"), rs.getString("nome"),
                rs.getString("email"), rs.getString("telefone"), rs.getString("tipo")
        );

        // 2. Cria o Livro (de forma simplificada, pois não é o foco principal aqui)
        Livro livro = new Livro(
                rs.getString("isbn"), null, rs.getString("titulo"), null, new ArrayList<>()
        );

        // 3. Cria o Exemplar
        Exemplar_livro exemplar = new Exemplar_livro(
                rs.getInt("id_livro"), livro, rs.getBoolean("status")
        );

        // 4. Finalmente, cria o Emprestimo
        Date dataDevolucaoSQL = rs.getDate("data_devolucao");
        return new Emprestimo(
                rs.getInt("idEmprestimo"),
                rs.getDate("dataEmprestimo").toLocalDate(),
                dataDevolucaoSQL != null ? dataDevolucaoSQL.toLocalDate() : null,
                usuario,
                exemplar
        );
    }

    @Override
    protected void addNestedEntity(Emprestimo mainEntity, ResultSet rs) throws SQLException{}

    private String getCompletaQuery() {
        return """
        SELECT
            em.idEmprestimo, em.dataEmprestimo, em.dataDevolucao,
            u.idUsuario, u.nome, u.email, u.telefone, u.tipo,
            ex.id_livro, ex.status,
            l.isbn, l.titulo
        FROM emprestimo em
        JOIN usuario u ON em.usuario_idUsuario = u.idUsuario
        JOIN exemplar_livro ex ON em.livro_id = ex.id_livro
        JOIN livro l ON ex.isbn_livro = l.isbn
        """;
    }

    // Os métodos de escrita (create, update, delete) não mudam.
    @Override
    public boolean create(Emprestimo emprestimo) throws SQLException {
        String sql = "INSERT INTO emprestimo (dataEmprestimo, usuario_idUsuario, livro_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(emprestimo.getDataEmprestimo()));
            stmt.setInt(2, emprestimo.getUsuario().getId());
            stmt.setInt(3, emprestimo.getLivro().getId());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM emprestimo WHERE idEmprestimo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}