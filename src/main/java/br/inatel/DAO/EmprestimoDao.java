package br.inatel.DAO;

import br.inatel.Biblioteca.*;
import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.time.LocalDate;
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
        String sql = getCompletaQuery() + " ORDER BY em.dataEmprestimo DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            // Chama o metodo da classe pai para fazer o trabalho pesado.
            return processResultSet(rs);
        }
    }

    // O metodo read também é simplificado.
    @Override
    public Emprestimo read(Integer id) throws SQLException {
        String sql = getCompletaQuery() + " WHERE em.idEmprestimo = ?";
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
        Date dataDevolucaoSQL = rs.getDate("dataDevolucao");
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

    @Override
    public boolean create(Emprestimo emprestimo) throws SQLException {
        // SQL para verificar o status atual do exemplar.
        String sqlCheckStatus = "SELECT status FROM exemplar_livro WHERE id_livro = ?";

        String sqlInsertEmprestimo = "INSERT INTO emprestimo (dataEmprestimo, usuario_idUsuario, livro_id) VALUES (?, ?, ?)";
        String sqlUpdateExemplar = "UPDATE exemplar_livro SET status = ? WHERE id_livro = ?";

        conn.setAutoCommit(false); // Inicia a transação

        try (PreparedStatement stmtCheck = conn.prepareStatement(sqlCheckStatus)) {
            // Passo 1: Verificar o status do exemplar
            int exemplarId = emprestimo.getLivro().getId();
            stmtCheck.setInt(1, exemplarId);

            try (ResultSet rs = stmtCheck.executeQuery()) {
                // Se não encontrou o exemplar ou se ele já está emprestado, aborte.
                if (!rs.next() || rs.getBoolean("status") == true) {
                    conn.rollback(); // Desfaz a transação (embora nada tenha sido feito ainda)
                    System.err.println("Operação abortada: O exemplar não existe ou já está emprestado.");
                    return false; // Retorna false para indicar falha
                }
            }

            // Se o código chegou até aqui, o exemplar está disponível. Prossiga.

            // Passo 2: Inserir o novo registro na tabela de empréstimos
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertEmprestimo)) {
                stmtInsert.setDate(1, java.sql.Date.valueOf(emprestimo.getDataEmprestimo()));
                stmtInsert.setInt(2, emprestimo.getUsuario().getId());
                stmtInsert.setInt(3, exemplarId);
                stmtInsert.executeUpdate();
            }

            // Passo 3: Atualizar o status do exemplar para emprestado (true)
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateExemplar)) {
                stmtUpdate.setBoolean(1, true);
                stmtUpdate.setInt(2, exemplarId);
                stmtUpdate.executeUpdate();
            }

            conn.commit(); // Confirma a transação
            return true;

        } catch (SQLException e) {
            conn.rollback();
            System.err.println("Erro ao criar empréstimo. A transação foi revertida.");
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Em: src/main/java/br/inatel/DAO/EmprestimoDao.java

    @Override
    public boolean delete(Integer idEmprestimo) throws SQLException {
        // Declaração dos SQLs que serão usados na transação
        String sqlSelectEmprestimo = "SELECT livro_id FROM emprestimo WHERE idEmprestimo = ?";
        String sqlDeleteEmprestimo = "DELETE FROM emprestimo WHERE idEmprestimo = ?";
        String sqlUpdateExemplar = "UPDATE exemplar_livro SET status = ? WHERE id_livro = ?";

        // Inicia a transação
        conn.setAutoCommit(false);
        int exemplarId = -1; // Variável para guardar o ID do exemplar

        try {
            // Passo 1: Descobrir qual exemplar está associado a este empréstimo
            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelectEmprestimo)) {
                stmtSelect.setInt(1, idEmprestimo);
                try (ResultSet rs = stmtSelect.executeQuery()) {
                    if (rs.next()) {
                        exemplarId = rs.getInt("livro_id");
                    } else {
                        // Se não existe empréstimo com esse ID, não há nada a fazer.
                        conn.rollback();
                        return false;
                    }
                }
            }

            // Passo 2: Deletar o registro do empréstimo
            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDeleteEmprestimo)) {
                stmtDelete.setInt(1, idEmprestimo);
                int affectedRows = stmtDelete.executeUpdate();
                if (affectedRows == 0) {
                    // Se o delete falhou por algum motivo, reverte.
                    conn.rollback();
                    return false;
                }
            }

            // Passo 3: Atualizar o status do exemplar para disponível (false)
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateExemplar)) {
                stmtUpdate.setBoolean(1, false); // false = disponível
                stmtUpdate.setInt(2, exemplarId);
                stmtUpdate.executeUpdate();
            }

            // Se todos os passos foram concluídos com sucesso, confirma a transação
            conn.commit();
            return true;

        } catch (SQLException e) {
            // Em caso de qualquer erro, desfaz a transação inteira
            conn.rollback();
            System.err.println("Erro ao deletar empréstimo. A transação foi revertida.");
            throw e;
        } finally {
            // Restaura o modo de auto-commit da conexão
            conn.setAutoCommit(true);
        }
    }
    public boolean update(int idEmprestimo, LocalDate dataDevolucao) throws SQLException {
        String sqlUpdateEmprestimo = "UPDATE emprestimo SET dataDevolucao = ? WHERE idEmprestimo = ?";
        try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdateEmprestimo)) {
            stmtUpdate.setDate(1, java.sql.Date.valueOf(dataDevolucao));
            stmtUpdate.setInt(2, idEmprestimo);
            int affectedRows = stmtUpdate.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            conn.commit();
        }
        return true;
    }
}