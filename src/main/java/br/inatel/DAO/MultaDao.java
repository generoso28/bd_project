package br.inatel.DAO;

import br.inatel.Biblioteca.*;
import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.util.List;

/**
 * DAO para a entidade complexa 'Multa'.
 * Herda de AbstractComplexDao para reutilizar a lógica de processamento de ResultSet
 * e implementa a interface Dao para as operações de CRUD.
 */
public class MultaDao extends AbstractComplexDao<Multa, Integer> implements Dao<Multa, Integer> {
    private final Connection conn;

    public MultaDao(Connection connection) {
        this.conn = connection;
    }

    // readAll agora é simples e consistente com os outros DAOs.
    @Override
    public List<Multa> readAll() throws SQLException {
        String sql = getCompletaQuery() + " ORDER BY m.valor DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Chama o metodo da classe pai para fazer o trabalho pesado.
            return processResultSet(rs);
        }
    }

    // read também é simplificado.
    @Override
    public Multa read(Integer id) throws SQLException {
        String sql = getCompletaQuery() + " WHERE m.id_multa = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Multa> multas = processResultSet(rs);
                return multas.isEmpty() ? null : multas.getFirst();
            }
        }
    }

    // MÉTODOS ABSTRATOS IMPLEMENTADOS:
    // Fornecemos os detalhes específicos que a classe abstrata precisa.

    @Override
    protected Integer getKeyFromResultSet(ResultSet rs) throws SQLException {
        return rs.getInt("idMulta");
    }

    @Override
    protected Multa mapRowToMainEntity(ResultSet rs) throws SQLException {
        // 1. Monta o Empréstimo associado (lógica similar ao EmprestimoDao)
        Usuario usuario = new Usuario(rs.getInt("idUsuario"), rs.getString("nome"), rs.getString("email"), null, null);
        Livro livro = new Livro(rs.getString("isbn"), null, rs.getString("titulo"), null, null);
        Exemplar_livro exemplar = new Exemplar_livro(rs.getInt("id_livro"), livro, rs.getBoolean("status"));
        Date dataDevolucaoSQL = rs.getDate("dataDevolucao");
        Emprestimo emprestimo = new Emprestimo(
                rs.getInt("idEmprestimo"),
                rs.getDate("dataEmprestimo").toLocalDate(),
                dataDevolucaoSQL != null ? dataDevolucaoSQL.toLocalDate() : null,
                usuario,
                exemplar
        );

        // 2. Monta a Multa, que é a entidade principal aqui
        return new Multa(
                emprestimo,
                rs.getDouble("valor"),
                rs.getDouble("juros"),
                rs.getInt("idMulta")
        );
    }

    @Override
    protected void addNestedEntity(Multa mainEntity, ResultSet rs) throws SQLException {}

    private String getCompletaQuery() {
        return """
        SELECT
            m.idMulta, m.multa, m.juros,
            em.idEmprestimo, em.dataEmprestimo, em.dataDevolucao,
            u.idUsuario, u.nome, u.email,
            ex.id_livro, ex.status,
            l.isbn, l.titulo
        FROM multa m
        JOIN emprestimo em ON m.emprestimo_idEmprestimo = em.idEmprestimo
        JOIN usuario u ON em.usuario_idUsuario = u.idUsuario
        JOIN exemplar_livro ex ON em.livro_id = ex.id_livro
        JOIN livro l ON ex.isbn_livro = l.isbn
        """;
    }

    // Os métodos de escrita (create, update, delete) permanecem inalterados.
    @Override
    public boolean create(Multa multa) throws SQLException {
        String sql = "INSERT INTO multa (multa, juros, emprestimo_idEmprestimo) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, multa.getMulta());
            stmt.setDouble(2, multa.getJuros());
            stmt.setInt(3, multa.getEmprestimo().getId());
            return stmt.executeUpdate() > 0;
        }
    }
    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM multa WHERE idMulta = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}