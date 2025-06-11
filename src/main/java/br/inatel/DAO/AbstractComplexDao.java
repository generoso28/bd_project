package br.inatel.DAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap; // Usamos LinkedHashMap para manter a ordem de inserção
import java.util.List;
import java.util.Map;

/**
 * Classe abstrata que implementa o padrão Template Method para DAOs complexos.
 * Ela gerencia o processo de montar objetos que têm relações um-para-muitos
 * a partir de um ResultSet.
 *
 * @param <T> O tipo da entidade principal (ex: Livro, Emprestimo).
 * @param <K> O tipo da chave primária da entidade principal (ex: String, Integer).
 */
public abstract class AbstractComplexDao<T, K> {

    /**
     * O "Template Method". Ele define o esqueleto do algoritmo de processamento.
     * Este metodo não pode ser sobrescrito (é final).
     */
    protected final List<T> processResultSet(ResultSet rs) throws SQLException {
        // Usamos um Map para agrupar as entidades filhas sob a entidade principal.
        Map<K, T> map = new LinkedHashMap<>();

        while (rs.next()) {
            // Pede para a subclasse extrair a chave da linha atual.
            K key = getKeyFromResultSet(rs);

            // Pega a entidade principal do mapa. Se não existir, pede para a subclasse criar.
            T mainEntity = map.computeIfAbsent(key, k -> {
                try {
                    return mapRowToMainEntity(rs);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            // Pede para a subclasse adicionar a entidade aninhada (o lado "muitos" da relação).
            addNestedEntity(mainEntity, rs);
        }
        return new ArrayList<>(map.values());
    }

    /**
     * As subclasses DEVEM implementar este metodo para extrair a chave primária
     * da entidade principal a partir de uma linha do ResultSet.
     * @param rs O ResultSet na linha atual.
     * @return A chave primária.
     */
    protected abstract K getKeyFromResultSet(ResultSet rs) throws SQLException;

    /**
     * As subclasses DEVEM implementar este metodo para criar a entidade principal
     * (o lado "um" da relação) a partir de uma linha do ResultSet.
     * @param rs O ResultSet na linha atual.
     * @return O objeto da entidade principal.
     */
    protected abstract T mapRowToMainEntity(ResultSet rs) throws SQLException;

    /**
     * As subclasses DEVEM implementar este metodo para criar e adicionar a entidade
     * aninhada (o lado "muitos") à entidade principal.
     * @param mainEntity A entidade principal onde a entidade aninhada será adicionada.
     * @param rs O ResultSet na linha atual.
     */
    protected abstract void addNestedEntity(T mainEntity, ResultSet rs) throws SQLException;
}